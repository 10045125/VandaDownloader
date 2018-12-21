package vanda.wzl.vandadownloader

import android.os.Environment
import android.text.TextUtils
import android.util.Log
import okhttp3.Request
import vanda.wzl.vandadownloader.net.OkHttpProxy
import vanda.wzl.vandadownloader.util.SpeedUtils
import java.io.File
import java.io.InputStream
import java.net.HttpURLConnection
import java.util.concurrent.Executors

class DownloadTaskSchedule : ExeProgressCalc {

    companion object {
        const val SEEK_SIZE = 3
        const val TYPE_CHUNKED = "chunked"
        const val SPEED_TIME_INTVAL = 1500
        const val PROCESS_TIME_INTVAL = 70
        const val ONE_SECEND_TIME = 1000 //ms
    }

    private val mThreadNum = 9
    private val mList = ArrayList<ExeRunnable>()
    private var mThreadPool = Executors.newFixedThreadPool(12)
    private var mFileSize: Long = -1
    private var isSupportSeek: Boolean = false
    private var mCurSofar = 0L
    private var mSpeedIncrement = 0L
    private var mTime = System.currentTimeMillis()

    fun start(url: String, downloadListener: DownloadListener) {
        mThreadPool.execute {
            startAync(url, downloadListener)
        }
    }

    private fun startAync(url: String, downloadListener: DownloadListener) {
        synchronized(lock = mList) {
            val inputStream = analysisFileAttributes(url)
            val exSize = mFileSize % mThreadNum
            val segmentSize = (mFileSize - exSize) / mThreadNum
            val threadNum = if (isSupportSeek) mThreadNum else 1

            Log.i("vanda", "mFileSize = $mFileSize exSize = $exSize  segmentSize = $segmentSize")

            val tempPath = Environment.getExternalStorageDirectory().absolutePath + "/weixin.apk"
            val file = File(tempPath)
            if (file.exists()) {
                file.delete()
            }
            file.createNewFile()

            for (i in 1..threadNum) {
                val downloadRunnable = DownloadRunnable(url, segmentSize, exSize, 0, if (i == 1) inputStream else null, i - 1, mFileSize, mThreadNum, this, downloadListener)
                mList.add(downloadRunnable)
            }

            for (downloadRunnable in mList) {
                mThreadPool.execute(downloadRunnable)
            }

        }
    }

    private fun analysisFileAttributes(url: String): InputStream? {
        val mRequestBuilder = Request.Builder()
        mRequestBuilder.addHeader("Range", "bytes=0-$SEEK_SIZE")
        var request = mRequestBuilder.url(url).get().build()
        var mCall = OkHttpProxy.instance.newCall(request)

        var mResponse = mCall.execute()
        val code = mResponse.code()

        // ------- 如果同时收到了Transfer-Encoding字段和Content-Length头字段，那么必须忽略Content-Length字段

        val transferEncoding = mResponse.header("Transfer-Encoding")

        if (TextUtils.equals(transferEncoding, TYPE_CHUNKED)) {
            isSupportSeek = false
        } else {
            isSupportSeek = code == HttpURLConnection.HTTP_PARTIAL && !TextUtils.isEmpty(mResponse.header("Content-Range"))
            val contentLength: Long? = mResponse.header("Content-Length")?.toLong()
            Log.e("vanda", "transferEncoding=$transferEncoding    content-length=$contentLength")
        }

        Log.e("vanda", "isSupportSeek=$isSupportSeek")

        if (TextUtils.isEmpty(transferEncoding)) {
            mRequestBuilder.addHeader("Range", "bytes=0-")
            request = mRequestBuilder.url(url).get().build()
            mCall = OkHttpProxy.instance.newCall(request)
            mResponse = mCall.execute()
            mFileSize = mResponse.header("Content-Length")?.toLong()!!

        }
        return mResponse.body()?.byteStream()
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
}