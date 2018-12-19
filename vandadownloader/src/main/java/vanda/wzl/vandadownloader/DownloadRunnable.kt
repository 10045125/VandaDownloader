package vanda.wzl.vandadownloader

import android.os.Environment
import okhttp3.Request
import okio.*
import vanda.wzl.vandadownloader.io.file.io.RandomAcessFileOutputStream
import vanda.wzl.vandadownloader.io.file.separation.GlobalSingleThreadWriteFileStream
import vanda.wzl.vandadownloader.io.file.separation.WriteSeparation
import vanda.wzl.vandadownloader.net.OkHttpProxy
import vanda.wzl.vandadownloader.progress.GlobalSingleThreadHandlerProgress
import vanda.wzl.vandadownloader.progress.ProgressData
import vanda.wzl.vandadownloader.status.OnStatus
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class DownloadRunnable(private val mUrl: String) : Runnable {

    companion object {
        private const val BUFFER_SIZE = 1024 * 8L
        private const val PROGRESS_INTVAL = 500 //ms
        private const val TIME_KEY = "time_key" //ms
    }

    private var mRandomAcessFileOutputStream: RandomAcessFileOutputStream? = null
    private val mQuarkBufferedSinkQueue = ConcurrentLinkedQueue<WriteSeparation>()
    private val mQuarkBufferedSinkListWait = ConcurrentLinkedQueue<WriteSeparation>()

    private var mTimes = ConcurrentHashMap<String, Long>()
    private var mIsCancel: Boolean = false

    private val mRandomAccessFile: RandomAccessFile
        @Throws(IOException::class)
        get() {
            val tempPath = Environment.getExternalStorageDirectory().absolutePath + "/wangzherongyao.apk"
            val file = File(tempPath)
            return RandomAccessFile(file, "rw")
        }

    override fun run() {
        val mRequestBuilder = Request.Builder()
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

    private fun close(source: Source?, outputStream: OutputStream) {
        try {
            source?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        try {
            outputStream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun fetch(inputStream: InputStream) {
        var source: Source? = null
        var sink: BufferedSink
        var buffer: Buffer
        val outputStream: OutputStream

        mTimes[TIME_KEY] = 0L

        if (mRandomAcessFileOutputStream == null) {
            mRandomAcessFileOutputStream = RandomAcessFileOutputStream(mRandomAccessFile)
        }

        outputStream = mRandomAcessFileOutputStream as RandomAcessFileOutputStream

        if (mQuarkBufferedSinkQueue.size == 0) {
            val writeSeparation = WriteSeparationImpl(mTimes, outputStream.sink().buffer(), mQuarkBufferedSinkQueue, mQuarkBufferedSinkListWait)
            mQuarkBufferedSinkQueue.offer(writeSeparation)
        }

        var writeSeparation: WriteSeparation? = mQuarkBufferedSinkQueue.poll()!!
        sink = writeSeparation!!.quarkBufferSink()
        mQuarkBufferedSinkListWait.offer(writeSeparation)

        try {
            sink.emit()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        buffer = sink.buffer()
        var sofar: Long = 0
        var len: Long = 0

        try {
            source = inputStream.source()
            len = source!!.read(buffer, BUFFER_SIZE)
            while (len != -1L) {
                sofar += len
                onCancel()
                handleProgressValue(writeSeparation!!, sofar, sofar, sofar, OnStatus.PROGRESS)
                onWrite(writeSeparation!!)
                writeSeparation = mQuarkBufferedSinkQueue.poll()
                writeSeparation = writeSeparation?.let { writeSeparation } ?: WriteSeparationImpl(mTimes, outputStream.sink().buffer(), mQuarkBufferedSinkQueue, mQuarkBufferedSinkListWait)
                mQuarkBufferedSinkListWait.offer(writeSeparation)
                sink = writeSeparation!!.quarkBufferSink()
                buffer = sink.buffer()
                len = source!!.read(buffer, BUFFER_SIZE)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            close(source, outputStream)
        }
    }

    private fun handleProgressValue(writeSeparation: WriteSeparation, sofar: Long, total: Long, id: Long, status: Int) {
        writeSeparation.sofar(sofar)
        writeSeparation.total(total)
        writeSeparation.id(id)
        writeSeparation.status(status)
    }

    private fun onCancel(): Boolean {
        return mIsCancel
    }

    private fun onWrite(writeSeparation: WriteSeparation) {
        GlobalSingleThreadWriteFileStream.ayncWrite(writeSeparation)
    }

    private class WriteSeparationImpl(internal val mTimes: ConcurrentHashMap<String, Long>, internal val mQuarkBufferedSink: BufferedSink, private val mQuarkBufferedSinkQueue: Queue<WriteSeparation>, private val mQuarkBufferedSinkQueueWait: Queue<WriteSeparation>) : WriteSeparation {
        private var sofar: Long = -1
        private var total: Long = -1
        private var id: Long = -1
        private var time: Long = 0
        private var status: Int = OnStatus.INVALID

        override fun time(time: Long) {
            this.time = time
        }

        override fun sofar(sofar: Long) {
            this.sofar = sofar
        }

        override fun total(total: Long) {
            this.total = total
        }

        override fun status(status: Int) {
            this.status = status
        }

        override fun id(id: Long) {
            this.id = id
        }

        override fun onWriteSegmentBytesToStore() {
            try {
                mQuarkBufferedSink.emit()

                if (System.currentTimeMillis() - mTimes[TIME_KEY]!! > PROGRESS_INTVAL) {
                    mTimes[TIME_KEY] = System.currentTimeMillis()
                    val progressData = ProgressData.obtain()
                    progressData.sofar = sofar
                    progressData.total = sofar
                    progressData.id = sofar
                    progressData.status = OnStatus.PROGRESS
                    GlobalSingleThreadHandlerProgress.ayncProgressData(progressData)
                }

            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        override fun syncCurData() {
            synchronized(WriteSeparationImpl::class.java) {
                if (mQuarkBufferedSinkQueueWait.contains(this)) {
                    mQuarkBufferedSinkQueue.offer(this)
                    mQuarkBufferedSinkQueueWait.remove(this)
                }
            }
        }

        override fun quarkBufferSink(): BufferedSink {
            return mQuarkBufferedSink
        }
    }
}
