package vanda.wzl.vandadownloader

import android.os.Environment
import android.util.Log
import okhttp3.Request
import quarkokio.buffer
import quarkokio.sink
import quarkokio.source
import vanda.wzl.vandadownloader.database.RemarkMultiThreadPointSqlEntry
import vanda.wzl.vandadownloader.database.RemarkPointSql
import vanda.wzl.vandadownloader.io.file.io.RandomAcessFileOutputStream
import vanda.wzl.vandadownloader.io.file.separation.GlobalSingleThreadWriteFileStream
import vanda.wzl.vandadownloader.io.file.separation.WriteSeparation
import vanda.wzl.vandadownloader.net.OkHttpProxy
import vanda.wzl.vandadownloader.status.OnStatus
import java.io.*
import java.util.concurrent.ConcurrentLinkedQueue

class DownloadRunnable(
        private val mUrl: String,
        private val mSegmentSize: Long,
        private val mExtSize: Long,
        private var mSofar: Long,
        private var mInputStream: InputStream?,
        private val mThreadSerialNumber: Int,
        private val mFileSize: Long,
        private val mThreadNumber: Int,
        private val mIsSupportMulti: Boolean,
        private val mExeProgressCalc: ExeProgressCalc,
        private val mDownloadListener: DownloadListener,
        private val mPath: String,
        private val mDownloadId: Long
) : ExeRunnable {

    companion object {
        private const val BUFFER_SIZE = 1024 * 8L
    }

    private var mRandomAcessFileOutputStream: RandomAcessFileOutputStream = RandomAcessFileOutputStream()
    private val mQuarkBufferedSinkQueue = ConcurrentLinkedQueue<WriteSeparation>()
    private val mQuarkBufferedSinkListWait = ConcurrentLinkedQueue<WriteSeparation>()

    private var mTimes = Array(2) { 0L }
    private var mIsCancel: Boolean = false
    private var mStartPosition = -1L
    private val mInitStartPosition: Long?
    private var mEndPosition = -1L
    private var mTotal = -1L
    private var mIsComplete = false
    private var mSeparationChunkSize = 0L

    init {
        mTotal = mFileSize
        mInitStartPosition = calcStartPosition()
        calcEndPosition()
        mSeparationChunkSize = if (mThreadSerialNumber == (mThreadNumber - 1)) {
            mSegmentSize + mExtSize
        } else mSegmentSize

        initDatabase()
    }

    private val mRandomAccessFile: RandomAccessFile
        @Throws(IOException::class)
        get() {
            val file = File(mPath)
            if (!file.exists()) {
                file.createNewFile()
            }
            return RandomAccessFile(file, "rw")
        }

    private fun initDatabase() {
        if (!mExeProgressCalc.remarkMultiThreadPointSqlEntry(mDownloadId, mThreadSerialNumber.toLong()).invalid) {
            val remarkMultiThreadPointSqlEntry = RemarkMultiThreadPointSqlEntry()
            remarkMultiThreadPointSqlEntry.fillingValue(-1, mUrl, mSofar, mTotal, OnStatus.PENGING, mThreadSerialNumber, mDownloadId, mSegmentSize, mExtSize)
            mExeProgressCalc.insert(remarkMultiThreadPointSqlEntry)
        }
    }

    private fun calcStartPosition(): Long {
        mStartPosition = if (isChunk()) {
            0
        } else {
            if (mStartPosition != -1L) {
                mStartPosition + mSofar
            } else {
                if (mSegmentSize > 0) mThreadSerialNumber * mSegmentSize + mSofar else -1
            }
        }
        mSofar = 0
        return mStartPosition
    }

    private fun calcEndPosition() {
        mEndPosition = if (mThreadSerialNumber == (mThreadNumber - 1)) {
            (mThreadSerialNumber + 1) * mSegmentSize + mExtSize
        } else (mThreadSerialNumber + 1) * mSegmentSize
    }

    private fun startPosition(): Long {
        return mStartPosition
    }

    private fun endPosition(): Long {
        return mEndPosition
    }

    override fun sofar(): Long {
        return mStartPosition + mSofar - mInitStartPosition!!
    }

    override fun complete(): Boolean {
        return mIsComplete
    }

    override fun run() {
        Log.i("vanda", "run num = $mThreadSerialNumber  startPosition = ${startPosition()} endPosition= ${endPosition()} mSofar = $mSofar")
        do {

            if (complete()) {
                break
            }

            calcStartPosition()

            if (startPosition() > 0 && endPosition() > 0 && startPosition() >= endPosition()) {
                mIsComplete = true
                break
            }

            if (mInputStream != null && mThreadSerialNumber == 0) {
                fetch(mInputStream!!)
            } else {
                val mRequestBuilder = Request.Builder()
                if (!isChunk() && startPosition() >= 0) {
                    val range = "bytes=${startPosition()}-" + if (mThreadSerialNumber == mThreadNumber - 1) "" else endPosition()
                    Log.i("vanda", "range = $range")
                    mRequestBuilder.addHeader("Range", range)
                }
                val request = mRequestBuilder.url(mUrl).get().build()
                val mCall = OkHttpProxy.instance.newCall(request)
                try {
                    val mResponse = mCall.execute()
                    if (mResponse.body() != null) {
                        fetch(mResponse.body()!!.source().inputStream())
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }

            mInputStream = null

        } while (true)
    }

    private fun fetch(inputStream: InputStream) {
        val source: quarkokio.Source?
        var sink: quarkokio.BufferedSink
        var buffer: quarkokio.Buffer

        val seek = startPosition()
        mRandomAcessFileOutputStream.updateOutputStream(mRandomAccessFile, seek)

        val outputStream: OutputStream = mRandomAcessFileOutputStream

        source = inputStream.source()

        if (mQuarkBufferedSinkQueue.size == 0) {
            mQuarkBufferedSinkQueue.offer(WriteSeparationImpl(mTimes, outputStream.sink().buffer(), source, outputStream, mQuarkBufferedSinkQueue, mQuarkBufferedSinkListWait))
        }

        var writeSeparation: WriteSeparation? = mQuarkBufferedSinkQueue.poll()!!
        sink = writeSeparation!!.quarkBufferSink()
        mQuarkBufferedSinkListWait.offer(writeSeparation)

        try {

            buffer = sink.buffer
            var len: Long
            len = source!!.read(buffer, BUFFER_SIZE)

            while (len != -1L) {
                mSofar += len
                onCancel()

                if (mSegmentSize > 0) {
                    mIsComplete = sofar() >= mSeparationChunkSize
                }

                handleAyncData(writeSeparation!!, mSofar, if (mIsComplete) OnStatus.COMPLETE else OnStatus.PROGRESS)

                if (mIsComplete || mSegmentSize > 0 && mThreadSerialNumber == 0 && mSofar >= mSegmentSize) {
                    break
                }

                writeSeparation = mQuarkBufferedSinkQueue.poll()
                writeSeparation = writeSeparation?.let { writeSeparation } ?: WriteSeparationImpl(mTimes, outputStream.sink().buffer(), source, outputStream, mQuarkBufferedSinkQueue, mQuarkBufferedSinkListWait)
                mQuarkBufferedSinkListWait.offer(writeSeparation)
                sink = writeSeparation!!.quarkBufferSink()
                buffer = sink.buffer
                len = source!!.read(buffer, BUFFER_SIZE)

                if (len < 0 && mSegmentSize < 0) {
                    mIsComplete = true
                    handleAyncData(writeSeparation!!, mSofar, OnStatus.COMPLETE)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun isComplete(): Boolean {
        return sofar() >= mSeparationChunkSize
    }

    private fun isChunk(): Boolean {
        return !mIsSupportMulti
    }

    private fun handleAyncData(writeSeparation: WriteSeparation, sofar: Long, status: Int) {
        handleProgressValue(writeSeparation!!, sofar, mTotal, sofar, mThreadSerialNumber, status)
        GlobalSingleThreadWriteFileStream.ayncWrite(writeSeparation!!)
    }

    private fun handleProgressValue(writeSeparation: WriteSeparation, sofar: Long, total: Long, id: Long, threadId: Int, status: Int) {
        writeSeparation.sofar(sofar)
        writeSeparation.total(total)
        writeSeparation.id(mDownloadId)
        writeSeparation.status(status)
        writeSeparation.threadId(threadId)
        writeSeparation.exeProgressCalc(mExeProgressCalc)
        writeSeparation.downloadListener(mDownloadListener)
        writeSeparation.segment(mSeparationChunkSize)
        writeSeparation.extSize(mExtSize)
        writeSeparation.url(mUrl)
        writeSeparation.path(mPath)
    }

    private fun onCancel(): Boolean {
        return mIsCancel
    }

}
