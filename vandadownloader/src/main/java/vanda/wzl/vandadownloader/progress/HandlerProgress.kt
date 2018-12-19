package vanda.wzl.vandadownloader.progress


import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import vanda.wzl.vandadownloader.status.OnStatus

class HandlerProgress(looper: Looper) : Handler(looper) {

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            MSG_WRITE -> proSegmentBytesToStoreSync(msg)
            MSG_QIUT -> {
            }
        }
    }

    private fun proSegmentBytesToStoreSync(msg: Message) {
        val progressData = msg.obj as ProgressData

        when (progressData.status) {
            OnStatus.PENGING -> {

            }

            OnStatus.START -> {

            }

            OnStatus.CONTECT -> {

            }

            OnStatus.PROGRESS -> {
                Log.d("vanda", "sofar = ${progressData.sofar}")
            }

            OnStatus.COMPLETE -> {

            }

            OnStatus.PAUSE -> {

            }

            OnStatus.ERROR -> {

            }

            OnStatus.RETRY -> {

            }
        }

        progressData.recycle()
    }

    companion object {
        internal val MSG_WRITE = 0x1101
        internal val MSG_QIUT = 0x1102
    }

}
