package vanda.wzl.vandadownloader

import vanda.wzl.vandadownloader.io.file.separation.WriteSeparation
import vanda.wzl.vandadownloader.progress.GlobalSingleThreadHandlerProgress
import vanda.wzl.vandadownloader.progress.ProgressData
import vanda.wzl.vandadownloader.status.OnStatus
import java.io.IOException
import java.io.OutputStream
import java.util.*

internal class WriteSeparationImpl(
        private val mTimes: Array<Long>,
        private val mQuarkBufferedSink: quarkokio.BufferedSink,
        private val mSource: quarkokio.Source?,
        private val mOutputStream: OutputStream?,
        private val mQuarkBufferedSinkQueue: Queue<WriteSeparation>,
        private val mQuarkBufferedSinkQueueWait: Queue<WriteSeparation>
) : WriteSeparation {

    private var sofar: Long = -1
    private var total: Long = -1
    private var id: Long = -1
    private var time: Long = 0
    private var threadId: Int = 0
    private var segment: Long = 0
    private var status: Int = OnStatus.INVALID
    private var exeProgressCalc: ExeProgressCalc? = null
    private var downloadListener: DownloadListener? = null

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

    override fun threadId(id: Int) {
        this.threadId = id
    }

    override fun exeProgressCalc(exeProgressCalc: ExeProgressCalc) {
        this.exeProgressCalc = exeProgressCalc
    }

    override fun downloadListener(downloadListener: DownloadListener) {
        this.downloadListener = downloadListener
    }

    override fun segment(segment: Long) {
        this.segment = segment
    }

    private fun progressIntval(): Int {
        return PROGRESS_INTVAL
    }

    override fun onWriteSegmentBytesToStore() {
        try {
            mQuarkBufferedSink.emit()

            val intval = System.currentTimeMillis() - mTimes[0]
            if (status == OnStatus.COMPLETE || intval > progressIntval()) {
                mTimes[1] = sofar
                mTimes[0] = System.currentTimeMillis()

                if (status == OnStatus.COMPLETE) {
                    mSource?.close()
                    mOutputStream?.close()
                }

                val progressData = ProgressData.obtain()
                progressData.sofar = sofar
                progressData.total = total
                progressData.id = sofar
                progressData.threadId = threadId
                progressData.status = status
                progressData.exeProgressCalc = exeProgressCalc
                progressData.segment = segment
                progressData.downloadListener = downloadListener
                GlobalSingleThreadHandlerProgress.ayncProgressData(progressData)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun syncCurData() {
        synchronized(mQuarkBufferedSinkQueueWait) {
            if (mQuarkBufferedSinkQueueWait.contains(this)) {
                mQuarkBufferedSinkQueue.offer(this)
                mQuarkBufferedSinkQueueWait.remove(this)
            }
        }
    }

    override fun quarkBufferSink(): quarkokio.BufferedSink {
        return mQuarkBufferedSink
    }

    companion object {
        private const val PROGRESS_INTVAL = 100 //ms
    }
}