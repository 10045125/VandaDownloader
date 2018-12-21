package vanda.vandadownloader

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView
import io.victoralbertos.breadcumbs_view.BreadcrumbsView
import vanda.wzl.vandadownloader.DownloadListener
import vanda.wzl.vandadownloader.DownloadTaskSchedule
import vanda.wzl.vandadownloader.util.SpeedUtils

class MainActivity : AppCompatActivity(), DownloadListener {

    private val url = "http://dlied5.myapp.com/myapp/1104466820/sgame/2017_com.tencent.tmgp.sgame_h177_1.42.1.6_a6157f.apk"
//    private val url: String = "https://dldir1.qq.com/weixin/android/weixin673android1360.apk"


    private var cacheCurrentStep: Int? = null
    private var breadcrumbsView: BreadcrumbsView? = null
    private var mTextViewTitle: TextView? = null
    private var mTextViewProgress: TextView? = null
    private var mTextViewSpeed: TextView? = null
    private var mTextViewTime: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_breadcrumbs)

        //Survive config changes.
        if (lastCustomNonConfigurationInstance == null) {
            cacheCurrentStep = 0
        } else {
            cacheCurrentStep = lastCustomNonConfigurationInstance as Int
        }

        breadcrumbsView = findViewById(R.id.breadcrumbs)
        mTextViewTitle = findViewById(R.id.title)
        mTextViewTitle!!.text = "王者荣耀.apk"
        mTextViewProgress = findViewById(R.id.progress)
        mTextViewSpeed = findViewById(R.id.speed)
        mTextViewTime = findViewById(R.id.time)

        //        findViewById(R.id.bt_next).setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View view) {
        //                breadcrumbsView.nextStep(0, 0.9f);
        //                breadcrumbsView.nextStep(1, 0.8f);
        //                breadcrumbsView.nextStep(2, 0.7f);
        //                breadcrumbsView.nextStep(3, 0.6f);
        //                breadcrumbsView.nextStep(4, 0.5f);
        //            }
        //        });
        //
        //        findViewById(R.id.bt_prev).setOnClickListener(new View.OnClickListener() {
        //            @Override
        //            public void onClick(View view) {
        //                breadcrumbsView.prevStep(0);
        //                breadcrumbsView.prevStep(1);
        //                breadcrumbsView.prevStep(2);
        //                breadcrumbsView.prevStep(3);
        //                breadcrumbsView.prevStep(4);
        //            }
        //        });

        testDownload()
    }

    private fun testDownload() {
        val downloadTaskSchedule = DownloadTaskSchedule()
        downloadTaskSchedule.start(url, this)
    }

    override fun progress(sofar: Long, total: Long, childPercent: String, percent: String, threadId: Int, speed: String) {
        breadcrumbsView!!.nextStep(threadId, java.lang.Float.valueOf(childPercent))
        mTextViewProgress!!.text = String.format("%s/%s", SpeedUtils.formatSize(sofar), SpeedUtils.formatSize(total))
        mTextViewSpeed!!.text = String.format("%s/s", speed)
    }

}
