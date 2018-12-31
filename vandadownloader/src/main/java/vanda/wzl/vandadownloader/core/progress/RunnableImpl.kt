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

import android.util.Log
import vanda.wzl.vandadownloader.core.handler.MainHandler

class RunnableImpl(var progressData: ProgressData) : Runnable {
    private var mNext: RunnableImpl? = null

    fun reset() {
    }

    fun recycle() {
        reset()
        synchronized(sPoolSync) {
            if (sPoolSize < MAX_POOL_SIZE) {
                mNext = sPool
                sPool = this
                sPoolSize++
            }
        }
    }

    override fun run() {
        progressData(progressData)
        recycle()
    }

    private fun progressData(progressData: ProgressData) {
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
                Log.d("vanda", "id = ${progressData.id} sofarChild = ${progressData.sofarChild} segment = ${progressData.totalChild} totalProgress = $sofar  percent = $percent percentChild = $percentChild speed = $speed speedChild = ${progressData.speedChild}  threadId = ${progressData.threadId}")
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
        private const val FORMAT = "%.2f"
        private const val MAX_POOL_SIZE = 300
        private val sPoolSync = Any()
        private var sPoolSize = 0
        private var sPool: RunnableImpl? = null

        fun obtain(progressData: ProgressData): RunnableImpl {
            synchronized(sPoolSync) {
                return sPool?.let {
                    val m = sPool
                    sPool = m!!.mNext
                    m.mNext = null
                    sPoolSize--
                    m.reset()
                    m
                } ?: RunnableImpl(progressData)
            }
        }
    }
}