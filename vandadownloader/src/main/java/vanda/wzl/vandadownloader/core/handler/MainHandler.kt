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

package vanda.wzl.vandadownloader.core.handler

import android.os.Handler
import android.os.Looper
import android.os.Message
import vanda.wzl.vandadownloader.core.progress.ProgressData
import vanda.wzl.vandadownloader.core.status.OnStatus

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
                vanda.wzl.vandadownloader.core.status.OnStatus.PENGING -> {
                }

                vanda.wzl.vandadownloader.core.status.OnStatus.START -> {
                }

                vanda.wzl.vandadownloader.core.status.OnStatus.CONTECT -> {
                }

                vanda.wzl.vandadownloader.core.status.OnStatus.PROGRESS -> {
                    progress(progressData)
                }

                vanda.wzl.vandadownloader.core.status.OnStatus.COMPLETE -> {
                    progress(progressData)
                }

                vanda.wzl.vandadownloader.core.status.OnStatus.PAUSE -> {
                    pause(progressData)
                }

                vanda.wzl.vandadownloader.core.status.OnStatus.ERROR -> {
                }

                vanda.wzl.vandadownloader.core.status.OnStatus.RETRY -> {
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