package vanda.wzl.vandadownloader.handler

import android.os.Handler
import android.os.Looper
import android.os.Message
import vanda.wzl.vandadownloader.progress.ProgressData

class MainHandler {

    private val mHandler: Handler

    private object SingleHolder {
        internal val INSTANCE = MainHandler()
    }

    init {
        mHandler = MHandler()
    }

    companion object {

        private const val MSG_PRO_DATA = 0x1111

        fun syncProgressDataToMain(progressData: ProgressData) {
            val msg = Message.obtain()
            msg.what = MSG_PRO_DATA
            msg.obj = progressData
            SingleHolder.INSTANCE.mHandler.sendMessage(msg)
        }

        private fun progressData(msg: Message) {
            val progressData = msg.obj as ProgressData

            progressData.downloadListener?.progress(progressData.totalProgress, progressData.total, progressData.percentChild, progressData.percent, progressData.threadId, progressData.speed)

            progressData.recycle()
        }
    }

    private class MHandler : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_PRO_DATA -> progressData(msg)
            }
        }
    }

}