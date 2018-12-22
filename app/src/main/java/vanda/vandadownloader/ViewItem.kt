package vanda.vandadownloader

import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.white.progressview.HorizontalProgressView

class ViewItem(context: Context?) : LinearLayout(context) {
    var progressBar: HorizontalProgressView? = null
    var textDesc: TextView? = null
    var textSpeed: TextView? = null

    init {
        orientation = LinearLayout.VERTICAL
        init()
    }

    private fun init() {
        val relativeLayout = RelativeLayout(context)

        textDesc = TextView(context)
        textSpeed = TextView(context)

        val params = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        params.addRule(RelativeLayout.CENTER_VERTICAL)
        params.addRule(RelativeLayout.ALIGN_START)
        relativeLayout.addView(textDesc, params)

        val paramsSpeed = RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        paramsSpeed.addRule(RelativeLayout.CENTER_VERTICAL)
        paramsSpeed.addRule(RelativeLayout.ALIGN_PARENT_RIGHT)
        relativeLayout.addView(textSpeed, paramsSpeed)

        addView(relativeLayout)

        progressBar = HorizontalProgressView(context)
        progressBar!!.max = 100

        addView(progressBar)
    }

    fun title(title: String) {
        textDesc?.text = title
    }

    fun speed(speed: String) {
        textSpeed?.text = speed
    }

    @RequiresApi(Build.VERSION_CODES.N)
    fun progress(progress: Float) {
        progressBar?.setProgress((progress * 100).toInt(), true)
    }
}