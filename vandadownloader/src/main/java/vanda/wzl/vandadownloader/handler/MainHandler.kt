package vanda.wzl.vandadownloader.handler

import android.os.Handler
import android.os.Looper
import android.os.Message
import vanda.wzl.vandadownloader.progress.ProgressData
import vanda.wzl.vandadownloader.status.OnStatus

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

        fun syncCompleteProgressDataToMain(progressData: ProgressData) {
            val msg = Message.obtain()
            msg.what = MSG_PRO_DATA
            msg.obj = progressData
            SingleHolder.INSTANCE.mHandler.sendMessage(msg)
        }

        private fun progressData(msg: Message) {
            val progressData = msg.obj as ProgressData

            when (progressData.status) {
                OnStatus.PENGING -> {
                }

                OnStatus.START -> {
                }

                OnStatus.CONTECT -> {
                }

                OnStatus.PROGRESS -> {
                    progress(progressData)
                }

                OnStatus.COMPLETE -> {
                    progress(progressData)
                }

                OnStatus.PAUSE -> {
                    pause(progressData)
                }

                OnStatus.ERROR -> {
                }

                OnStatus.RETRY -> {
                }
            }

            progressData.recycle()
        }

        private fun pause(progressData: ProgressData) {
            progressData.downloadListener?.onPause()
        }

        private fun progress(progressData: ProgressData) {
            progressData.downloadListener?.onProgress(
                    progressData.sofar,
                    progressData.sofarChild,
                    progressData.total,
                    progressData.totalChild,
                    progressData.percent,
                    progressData.percentChild,
                    progressData.speed,
                    progressData.speedChild,
                    progressData.threadId
            )
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