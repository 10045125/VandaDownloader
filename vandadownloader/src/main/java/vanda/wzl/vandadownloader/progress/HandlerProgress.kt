package vanda.wzl.vandadownloader.progress


import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import vanda.wzl.vandadownloader.handler.MainHandler
import vanda.wzl.vandadownloader.status.OnStatus

class HandlerProgress(looper: Looper) : Handler(looper) {
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            MSG_WRITE -> progressData(msg)
            MSG_QIUT -> {
            }
        }
    }

    private fun progressData(msg: Message) {
        val progressData = msg.obj as ProgressData
        val totalProgress = progressData.exeProgressCalc?.exeProgressCalc()
        val speed = progressData.exeProgressCalc?.speedIncrement()
        val percent = String.format(FORMAT, totalProgress!! / progressData.total.toFloat())
        val percentChild = String.format(FORMAT, progressData.sofar / progressData.segment.toFloat())

        progressData.totalProgress = totalProgress
        progressData.speed = speed!!
        progressData.percent = percent
        progressData.percentChild = percentChild

        when (progressData.status) {
            OnStatus.PENGING -> {
                progressData.recycle()
            }

            OnStatus.START -> {
                progressData.recycle()
            }

            OnStatus.CONTECT -> {
                progressData.recycle()
            }

            OnStatus.PROGRESS -> {
                Log.d("vanda", "sofar = ${progressData.sofar} segment = ${progressData.segment} totalProgress = $totalProgress  percent = $percent percentChild = $percentChild speed = $speed  threadId = ${progressData.threadId}")
                MainHandler.syncProgressDataToMain(progressData)
            }

            OnStatus.COMPLETE -> {
                val allComplete = progressData.exeProgressCalc?.allComplete()
                Log.d("vanda", "sofar = ${progressData.sofar} segment = ${progressData.segment} percent = $percent percentChild = $percentChild threadId = ${progressData.threadId} complete, allComplete = $allComplete")

                progressData.recycle()
            }

            OnStatus.PAUSE -> {
                progressData.recycle()
            }

            OnStatus.ERROR -> {
                progressData.recycle()
            }

            OnStatus.RETRY -> {
                progressData.recycle()
            }
        }
    }

    companion object {
        internal val MSG_WRITE = 0x1101
        internal val MSG_QIUT = 0x1102
        private const val FORMAT = "%.2f"
    }

}
