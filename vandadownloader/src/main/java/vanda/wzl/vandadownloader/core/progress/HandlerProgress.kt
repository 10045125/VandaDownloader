/*
 * Copyright (c) 2018 YY Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vanda.wzl.vandadownloader.core.progress


import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import vanda.wzl.vandadownloader.core.handler.MainHandler
import vanda.wzl.vandadownloader.core.status.OnStatus
import vanda.wzl.vandadownloader.core.threadpool.AutoAdjustThreadPool

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
        val sofar = progressData.exeProgressCalc?.exeProgressCalc()
        val speed = progressData.exeProgressCalc?.speedIncrement()
        val percent = String.format(FORMAT, sofar!! / progressData.total.toFloat())
        val percentChild = String.format(FORMAT, progressData.sofarChild / progressData.totalChild.toFloat())

        progressData.sofar = sofar
        progressData.speed = speed!!
        progressData.percent = percent
        progressData.percentChild = percentChild

        when (progressData.status) {
            vanda.wzl.vandadownloader.core.status.OnStatus.PENGING -> {
                progressData.recycle()
            }

            vanda.wzl.vandadownloader.core.status.OnStatus.START -> {
                progressData.recycle()
            }

            vanda.wzl.vandadownloader.core.status.OnStatus.CONTECT -> {
                progressData.recycle()
            }

            vanda.wzl.vandadownloader.core.status.OnStatus.PROGRESS -> {
                Log.d("vanda", "sofarChild = ${progressData.sofarChild} segment = ${progressData.totalChild} totalProgress = $sofar  percent = $percent percentChild = $percentChild speed = $speed speedChild = ${progressData.speedChild}  threadId = ${progressData.threadId}")
                progressData.exeProgressCalc?.update(progressData.fillingRemarkMultiThreadPointSqlEntry())
                progressData.exeProgressCalc?.update(progressData.fillingRemarkPointSqlEntry())
                MainHandler.syncProgressDataToMain(progressData)
            }

            vanda.wzl.vandadownloader.core.status.OnStatus.COMPLETE -> {
                val allComplete = progressData.exeProgressCalc?.allComplete()
                Log.d("vanda", "sofarChild = ${progressData.sofarChild} segment = ${progressData.totalChild} percent = $percent percentChild = $percentChild threadId = ${progressData.threadId} complete, allComplete = $allComplete")

                progressData.exeProgressCalc?.update(progressData.fillingRemarkMultiThreadPointSqlEntry())
                progressData.exeProgressCalc?.update(progressData.fillingRemarkPointSqlEntry())

                MainHandler.syncProgressDataToMain(progressData)
                if (allComplete!!) {
                    progressData.allComplete = true
                    progressData.exeProgressCalc?.deleteThreadInfo(progressData.id)
                    MainHandler.syncCompleteProgressDataToMain(progressData)

                    postDelayed({
                        AutoAdjustThreadPool.stop()
                    }, 10 * 1000)
                }
            }

            vanda.wzl.vandadownloader.core.status.OnStatus.PAUSE -> {

                Log.d("vanda", "sofarChild = ${progressData.sofarChild} segment = ${progressData.totalChild} percent = $percent percentChild = $percentChild threadId = ${progressData.threadId} pause")

                progressData.exeProgressCalc?.update(progressData.fillingRemarkMultiThreadPointSqlEntry())
                progressData.exeProgressCalc?.update(progressData.fillingRemarkPointSqlEntry())

                progressData.exeProgressCalc?.pauseComplete(progressData.threadId)
                val allPauseComplete = progressData.exeProgressCalc?.allPauseComplete()
                Log.d("vanda", "sofarChild = ${progressData.sofarChild} segment = ${progressData.totalChild} percent = $percent percentChild = $percentChild threadId = ${progressData.threadId} pause, allPauseComplete = $allPauseComplete")
                if (allPauseComplete!!) {
                    MainHandler.syncProgressDataToMain(progressData)
                } else {
                    progressData.recycle()
                }
            }

            vanda.wzl.vandadownloader.core.status.OnStatus.ERROR -> {
                progressData.recycle()
            }

            vanda.wzl.vandadownloader.core.status.OnStatus.RETRY -> {
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
