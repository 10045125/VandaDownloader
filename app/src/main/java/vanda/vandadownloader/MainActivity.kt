package vanda.vandadownloader

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.AppCompatSeekBar
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import io.victoralbertos.breadcumbs_view.BreadcrumbsView
import vanda.wzl.vandadownloader.VandaDownloader
import vanda.wzl.vandadownloader.core.DownloadListener
import vanda.wzl.vandadownloader.core.util.DownloadUtils
import vanda.wzl.vandadownloader.core.util.SpeedUtils
import vanda.wzl.vandadownloader.multitask.DownloadTask
import vanda.wzl.vandadownloader.multitask.MultiDownloadTaskDispather

class MainActivity : AppCompatActivity() {

    private var url_wangzhe = "http://dlied5.myapp.com/myapp/1104466820/sgame/2017_com.tencent.tmgp.sgame_h177_1.42.1.6_a6157f.apk"
    private val url_weixin = "https://dldir1.qq.com/weixin/android/weixin673android1360.apk"
    private val url_uc = "https://wap3.ucweb.com/files/UCBrowser/zh-cn/999/UCBrowser_V12.2.4.1004_android_pf145_(Build181221104439).apk?auth_key=1546841763-0-0-027797a9e742e60492129100eb7049aa&SESSID=5d7eb191-4076-4d42-8867-f8349374294c"
    private val url_qq = "https://qd.myapp.com/myapp/qqteam/AndroidQQ/mobileqq_android.apk"
    private val url_taobao = "http://download.alicdn.com/wireless/taobao4android/latest/702757.apk"
//    private val url = "https://aq.qq.com/cn2/manage/mbtoken/mbtoken_download?Android=1&source_id=2886"
//    private val url = "https://aq.qq.com/cn2/manage/mbtoken/mbtoken_download?Android=1&source_id=2886"
//    private val url = "http://cn.club.vmall.com/forum.php?mod=attachment&aid=MjkzMzgwOXxiODViYzM3MnwxNDg5NTEyMzcxfDcxMjE1OTh8NTc4MjA3Mw%3D%3D"

    private var breadcrumbsView: BreadcrumbsView? = null
    private var mTextViewTitle: TextView? = null
    private var mTextViewProgress: TextView? = null
    private var mTextViewSpeed: TextView? = null
    private var mTextViewTime: TextView? = null
    private var mAppCompatSeekBar: AppCompatSeekBar? = null
    private var mTextViewThreadNum: TextView? = null

    private var mBtn: Button? = null
    private var mBtnClean: Button? = null
    private var mBtnDelete: Button? = null

    private var mAdapter: RecyclerViewAdapter? = null
    private var mRecyclerView: RecyclerView? = null

    private var mThreadNum = 3
    private var mIsStart = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breadcrumbs)

        mAppCompatSeekBar = findViewById(R.id.seekBar)
        mTextViewThreadNum = findViewById(R.id.numthread)
        mBtn = findViewById(R.id.bt_prev)
        mBtnClean = findViewById(R.id.bt_next)
        mBtnDelete = findViewById(R.id.bt_delete)
        breadcrumbsView = findViewById(R.id.breadcrumbs)
        mTextViewTitle = findViewById(R.id.title)
        mTextViewTitle!!.text = "王者荣耀.apk"
        mTextViewProgress = findViewById(R.id.progress)
        mTextViewSpeed = findViewById(R.id.speed)
        mTextViewTime = findViewById(R.id.time)

        mTextViewThreadNum!!.text = "thread num (1)"

        mAppCompatSeekBar!!.max = 128
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mAppCompatSeekBar!!.min = 1
        }
        mAppCompatSeekBar!!.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                mThreadNum = if (i == 0) 1 else i
                mTextViewThreadNum!!.text = "thread num ($mThreadNum)"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                mThreadNum = if (seekBar.progress == 0) 1 else seekBar.progress
            }
        })

        mAppCompatSeekBar!!.progress = 3


        mBtn!!.setOnClickListener {

            if (mIsStart) {
                pause()
                mBtn!!.text = "Start"
                mIsStart = false
            } else {
                start()
                mBtn!!.text = "Pause"
                mIsStart = true
            }
        }

        mBtnClean!!.setOnClickListener {
            VandaDownloader.clean(DownloadUtils.generateId(url_wangzhe, path_wangzhe))
            VandaDownloader.clean(DownloadUtils.generateId(url_weixin, path_weixin))
            VandaDownloader.clean(DownloadUtils.generateId(url_uc, path_uc))
            VandaDownloader.clean(DownloadUtils.generateId(url_qq, path_qq))
            VandaDownloader.clean(DownloadUtils.generateId(url_taobao, path_taobao))
        }

        mBtnDelete!!.setOnClickListener {
            VandaDownloader.deletefile()
        }

        pp()
    }

    fun start() {
        breadcrumbsView!!.setSegmentNum(mThreadNum)
        initRecyclerView()
        testDownload()
    }

    private fun pause() {
        VandaDownloader.pause(DownloadUtils.generateId(url_wangzhe, path_wangzhe))
        VandaDownloader.pause(DownloadUtils.generateId(url_weixin, path_weixin))
        VandaDownloader.pause(DownloadUtils.generateId(url_uc, path_uc))
        VandaDownloader.pause(DownloadUtils.generateId(url_qq, path_qq))
        VandaDownloader.pause(DownloadUtils.generateId(url_taobao, path_taobao))
    }

    val path_weixin = Environment.getExternalStorageDirectory().absolutePath + "/weixin_.apk"
    val path_wangzhe = Environment.getExternalStorageDirectory().absolutePath + "/wangzherongyao.apk"
    val path_uc = Environment.getExternalStorageDirectory().absolutePath + "/uc.apk"
    val path_qq = Environment.getExternalStorageDirectory().absolutePath + "/qq.apk"
    val path_taobao = Environment.getExternalStorageDirectory().absolutePath + "/taobao.apk"

    private fun testDownload() {
        val request = VandaDownloader.Request.Builder()
                .url(url_wangzhe)
                .path(path_wangzhe)
                .threadNum(3)
                .build()

        val mDownloadTask = VandaDownloader.createDownloadTask(request).addOnStateChangeListener(object : DownloadListener {
            @SuppressLint("SetTextI18n")
            override fun onProgress(sofar: Long, sofarChild: Long, total: Long, totalChild: Long, percent: Float, percentChild: Float, speed: Long, speedChild: Long, threadId: Int) {
                breadcrumbsView!!.nextStep(threadId, java.lang.Float.valueOf(percentChild))
                mTextViewProgress!!.text = String.format("%s/%s", SpeedUtils.formatSize(sofar), SpeedUtils.formatSize(total))
                mTextViewSpeed!!.text = if (percent < 1f) String.format("%s/s", calcSpeed(speed)) else "complete"
                mTextViewTime!!.text = "${(sofar * 100 / total).toInt()}%"

                val itemData = mAdapter!!.getItemData(threadId)
                itemData.title = "Segment$threadId (${SpeedUtils.formatSize(threadId * totalChild)} ~ ${SpeedUtils.formatSize((threadId + 1) * totalChild)})"
                itemData.progress = percentChild
                itemData.speed = if (percentChild < 1f) String.format("%s/s", calcSpeed(speedChild)) else "complete"

                mAdapter!!.notifyItemChanged(threadId)
            }

            override fun onComplete() {
//            activity.mIsStart = false
            }

            override fun onPause() {
                Log.i("vanda", "onPause complete")
//            activity.mIsStart = false
            }

        })

        mDownloadTask.start()
    }

    private fun initRecyclerView() {
        mRecyclerView = findViewById(R.id.recyclerview)
        mRecyclerView!!.layoutManager = LinearLayoutManager(this)

        mAdapter = RecyclerViewAdapter()

        val list = ArrayList<ItemData>()

        for (i in 1..mThreadNum) {
            val itemData = ItemData("Segment${(i - 1)}", "0KB/s", 0.00f)
            list.add(itemData)
        }

        mAdapter!!.setList(list)
        mRecyclerView!!.adapter = mAdapter
        mAdapter!!.notifyDataSetChanged()

    }

    private fun pp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.SYSTEM_ALERT_WINDOW), 199)
        }
    }

    companion object {
        private const val SIZE_1M = 1024 * 1024f
        private const val SIZE_MB = "MB"
        private const val SIZE_KB = "KB"
        private const val FORMAT = "%.2f"
        internal fun calcSpeed(speed: Long): String {
            return if (speed >= SIZE_1M) {
                String.format(FORMAT, (speed / SIZE_1M)) + SIZE_MB
            } else {
                String.format(FORMAT, (speed / 1024f)) + SIZE_KB
            }
        }
    }

}
