package vanda.wzl.vandadownloader

import vanda.wzl.vandadownloader.io.file.separation.WriteSeparation
import vanda.wzl.vandadownloader.progress.GlobalSingleThreadHandlerProgress
import vanda.wzl.vandadownloader.progress.ProgressData
import vanda.wzl.vandadownloader.status.OnStatus
import vanda.wzl.vandadownloader.util.SpeedUtils
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

            val intval = System.currentTimeMillis() - mTimes[0] + 1
            if (status == OnStatus.COMPLETE || intval > progressIntval()) {

                val curSofar = exeProgressCalc?.let { exeProgressCalc!!.sofar(threadId) } ?: 0
                val mSpeedIncrement = (curSofar - mTimes[1]) * ONE_SECEND_TIME / intval

                mTimes[1] = curSofar
                mTimes[0] = System.currentTimeMillis()

                if (status == OnStatus.COMPLETE) {
                    mSource?.close()
                    mOutputStream?.close()
                }


                val progressData = ProgressData.obtain()
                progressData.sofarChild = sofar
                progressData.total = total
                progressData.totalChild = segment
                progressData.id = sofar
                progressData.threadId = threadId
                progressData.speedChild = SpeedUtils.formatSize(mSpeedIncrement)
                progressData.status = status
                progressData.exeProgressCalc = exeProgressCalc
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
        private const val PROGRESS_INTVAL = 1000 //ms
        private const val ONE_SECEND_TIME = 1000 //ms
    }
}