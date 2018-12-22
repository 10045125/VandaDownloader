package vanda.wzl.vandadownloader

import android.os.Environment
import android.util.Log
import vanda.wzl.vandadownloader.net.ProviderNetFileTypeImpl
import vanda.wzl.vandadownloader.util.SpeedUtils
import java.io.File
import java.io.InputStream
import java.util.concurrent.Executors

class DownloadTaskSchedule(threadNum: Int) : ExeProgressCalc {

    companion object {
        const val SEEK_SIZE = 3
        const val TYPE_CHUNKED = "chunked"
        const val SPEED_TIME_INTVAL = 1500
        const val PROCESS_TIME_INTVAL = 70
        const val ONE_SECEND_TIME = 1000 //ms
    }

    private val mThreadNum = threadNum
    private val mList = ArrayList<ExeRunnable>()
    private var mThreadPool = Executors.newFixedThreadPool(threadNum + 1)
    private var mFileSize: Long = -1
    private var mIsSupportMutil: Boolean = false
    private var mCurSofar = 0L
    private var mSpeedIncrement = 0L
    private var mTime = System.currentTimeMillis()

    fun start(url: String, downloadListener: DownloadListener) {
        mThreadPool.execute {
            startAync(url, downloadListener)
        }
    }

    private fun handlerTaskParam(url: String): InputStream {
        val mProviderNetFileType = ProviderNetFileTypeImpl(url)
        val inputStream = mProviderNetFileType.firstIntactInputStream()
        mFileSize = mProviderNetFileType.fileSize()
        mIsSupportMutil = mProviderNetFileType.isSupportMutil()
        return inputStream
    }

    private fun createFile() {
        val tempPath = Environment.getExternalStorageDirectory().absolutePath + "/weixin.apk"
        val file = File(tempPath)
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
    }

    private fun exeTask(threadNum: Int, url: String, segmentSize: Long, exSize: Long, inputStream: InputStream, downloadListener: DownloadListener) {
        val thread = threadNum - 1
        for (i in 0..thread) {

            val downloadRunnable = DownloadRunnable(
                    url,
                    segmentSize,
                    exSize,
                    0,
                    if (i == 0) inputStream else null,
                    i,
                    mFileSize,
                    mThreadNum,
                    this,
                    downloadListener)

            mList.add(downloadRunnable)
        }

        for (downloadRunnable in mList) {
            mThreadPool.execute(downloadRunnable)
        }
    }

    private fun startAync(url: String, downloadListener: DownloadListener) {
        synchronized(lock = mList) {
            createFile()

            val inputStream = handlerTaskParam(url)
            val exSize = mFileSize % mThreadNum
            val segmentSize = (mFileSize - exSize) / mThreadNum
            val threadNum = if (mIsSupportMutil) mThreadNum else 1

            Log.i("vanda", "mFileSize = $mFileSize exSize = $exSize  segmentSize = $segmentSize")

            exeTask(threadNum, url, segmentSize, exSize, inputStream, downloadListener)
        }
    }

    override fun exeProgressCalc(): Long {
        synchronized(lock = mList) {
            var sofarTotal: Long = 0
            for (exeRunnable in mList) {
                sofarTotal += exeRunnable.sofar()
            }
            var time = System.currentTimeMillis() - mTime
            if (time >= SPEED_TIME_INTVAL) {
                time -= PROCESS_TIME_INTVAL
                mSpeedIncrement = (sofarTotal - mCurSofar) * ONE_SECEND_TIME / time
                mTime = System.currentTimeMillis()
                mCurSofar = sofarTotal
            }
            return sofarTotal
        }
    }

    override fun allComplete(): Boolean {
        synchronized(lock = mList) {
            var allComplete = true
            for (exeRunnable in mList) {
                if (!exeRunnable.complete()) {
                    allComplete = false
                }
            }
            return allComplete
        }
    }

    override fun speedIncrement(): String {
        return SpeedUtils.formatSize(mSpeedIncrement)
    }

    override fun sofar(curThreadId: Int): Long {
        synchronized(lock = mList) {
            return mList[curThreadId].sofar()
        }
    }
}