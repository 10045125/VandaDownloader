package vanda.wzl.vandadownloader

import android.content.Context
import android.os.Environment
import android.util.Log
import quarkokio.Segment
import quarkokio.SegmentPool
import vanda.wzl.vandadownloader.database.RemarkMultiThreadPointSqlEntry
import vanda.wzl.vandadownloader.database.RemarkPointSql
import vanda.wzl.vandadownloader.database.RemarkPointSqlEntry
import vanda.wzl.vandadownloader.database.RemarkPointSqlImpl
import vanda.wzl.vandadownloader.net.ProviderNetFileTypeImpl
import vanda.wzl.vandadownloader.status.OnStatus
import vanda.wzl.vandadownloader.threadpool.AutoAdjustThreadPool
import vanda.wzl.vandadownloader.util.DownloadUtils
import vanda.wzl.vandadownloader.util.SpeedUtils
import java.io.File
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

class DownloadTaskSchedule(threadNum: Int, context: Context) : AbstractDownloadTaskSchedule(context) {

    companion object {
        const val SEEK_SIZE = 3
        const val TYPE_CHUNKED = "chunked"
        const val SPEED_TIME_INTVAL = 1500
        const val PROCESS_TIME_INTVAL = 70
        const val ONE_SECEND_TIME = 1000 //ms
    }

    private var mThreadNum = threadNum
    private val mList = ConcurrentHashMap<Int, ExeRunnable>()
    private var mFileSize: Long = -1
    private var mIsSupportMulti: Boolean = false
    private var mCurSofar = 0L
    private var mSpeedIncrement = 0L
    private var mTime = System.currentTimeMillis()
    private var mPath: String = ""
    private var mDownloadId: Long = -1L
    private var mUrl: String = ""

    init {
        SegmentPool.MAX_SIZE = 1024 * 1024 * 3
        Segment.SIZE = 1024 * 256
    }

    fun start(url: String, downloadListener: DownloadListener) {
        AutoAdjustThreadPool.execute(Runnable {
            startAync(url, downloadListener)
        })
    }

    fun pause() {
        for (r in mList) {
            r.value.pause()
        }
    }

    private fun handlerTaskParam(url: String): InputStream {
        val mProviderNetFileType = ProviderNetFileTypeImpl(url)
        val inputStream = mProviderNetFileType.firstIntactInputStream()
        mFileSize = mProviderNetFileType.fileSize()
        mIsSupportMulti = mProviderNetFileType.isSupportMulti()
        return inputStream
    }

    private fun createFile() {
        mPath = Environment.getExternalStorageDirectory().absolutePath + "/weixin.apk"
        val file = File(mPath)
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    private fun initDatabaseInfo() {
        if (!remarkPointSqlEntry(mDownloadId).invalid) {
            val remarkPointSqlEntry = RemarkPointSqlEntry()
            remarkPointSqlEntry.fillingValue(mDownloadId, mUrl, mPath, 0, mFileSize, OnStatus.INVALID)
            insert(remarkPointSqlEntry)
        }
    }

    private fun exeTask(threadNum: Int, url: String, sofar: Long, segmentSize: Long, exSize: Long, inputStream: InputStream, downloadListener: DownloadListener) {
        val thread = threadNum - 1
        for (i in 0..thread) {

            val downloadRunnable = DownloadRunnable(
                    url,
                    segmentSize,
                    exSize,
                    sofar,
                    if (i == 0) inputStream else null,
                    i,
                    mFileSize,
                    mThreadNum,
                    mIsSupportMulti,
                    this,
                    downloadListener,
                    mPath,
                    mDownloadId)

            mList[i] = downloadRunnable
        }

        for (downloadRunnable in mList) {
            AutoAdjustThreadPool.execute(downloadRunnable.value)
        }
    }

    private fun startAync(url: String, downloadListener: DownloadListener) {

        mList.clear()

        createFile()
        mDownloadId = DownloadUtils.generateId(url, mPath).toLong()

        initDatabaseInfo()

        val threadInfos = findThreadInfo(mDownloadId)

        if (threadInfos.size == 0) {
            val inputStream = handlerTaskParam(url)
            val threadNum = if (mIsSupportMulti) mThreadNum else 1
            val exSize = mFileSize % threadNum
            val segmentSize = (mFileSize - exSize) / threadNum

            Log.i("vanda", "mFileSize = $mFileSize exSize = $exSize  segmentSize = $segmentSize")

            exeTask(threadNum, url, 0, segmentSize, exSize, inputStream, downloadListener)
        } else {
            mThreadNum = threadInfos.size
            for (threadinfo in threadInfos) {
                val downloadRunnable = DownloadRunnable(
                        url,
                        threadinfo.segment,
                        threadinfo.extSize,
                        threadinfo.sofar,
                        null,
                        threadinfo.threadId,
                        threadinfo.total,
                        threadInfos.size,
                        true,
                        this,
                        downloadListener,
                        mPath,
                        threadinfo.downloadFileId)

                mList[threadinfo.threadId] = downloadRunnable
            }

            for (downloadRunnable in mList) {
                AutoAdjustThreadPool.execute(downloadRunnable.value)
            }

        }
    }

    override fun exeProgressCalc(): Long {
        var sofarTotal: Long = 0
        for (exeRunnable in mList) {
            sofarTotal += exeRunnable.value.sofar()
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

    override fun allComplete(): Boolean {
        var allComplete = true
        for (exeRunnable in mList) {
            if (!exeRunnable.value.complete()) {
                allComplete = false
            }
        }
        return allComplete
    }

    override fun speedIncrement(): String {
        return SpeedUtils.formatSize(mSpeedIncrement)
    }

    override fun sofar(curThreadId: Int): Long {
        return mList[curThreadId]!!.sofar()
    }

    override fun pauseComplete(curThreadId: Int) {
        (mList[curThreadId])?.pauseComplete()
    }

    override fun allPauseComplete(): Boolean {
        var allPauseComplete = true
        for (exeRunnable in mList) {
            if (!exeRunnable.value.isPauseComplete()) {
                allPauseComplete = false
            }
        }
        return allPauseComplete
    }

    fun clean() {
        delete(mDownloadId)
        deleteThreadInfo(mDownloadId)
    }
}