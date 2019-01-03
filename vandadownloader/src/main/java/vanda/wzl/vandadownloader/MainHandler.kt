/*
 * Copyright (c) 2019 YY Inc
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

package vanda.wzl.vandadownloader

import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.Log
import android.util.SparseArray
import vanda.wzl.vandadownloader.multitask.DownloadListener
import vanda.wzl.vandadownloader.core.progress.ProgressData
import vanda.wzl.vandadownloader.core.status.OnStatus
import vanda.wzl.vandadownloader.multitask.DownloadTask
import java.util.concurrent.ConcurrentHashMap

class MainHandler {

    private val mHandler: Handler
    private val mDdownloadListenerSparseArray: SparseArray<DownloadListener> = SparseArray()
    private val mDownloadGlobalListener = ConcurrentHashMap<Int, DownloadListener>()

    private object SingleHolder {
        internal val INSTANCE = MainHandler()
    }

    init {
        mHandler = MHandler()
    }

    companion object {

        private const val MSG_PRO_DATA = 0x1111

        @Synchronized
        internal fun remarkDownloadListener(downloadTask: DownloadTask) {
            downloadTask.getOnStateChangeListener()?.let { SingleHolder.INSTANCE.mDdownloadListenerSparseArray.put(downloadTask.getId(), downloadTask.getOnStateChangeListener()) }
        }

        @Synchronized
        internal fun addGlobalDownloadListener(mDownloadListener: DownloadListener?) {
            mDownloadListener?.let { SingleHolder.INSTANCE.mDownloadGlobalListener[mDownloadListener.hashCode()] = mDownloadListener }
        }

        @Synchronized
        internal fun removeGlobalDownloadListener(mDownloadListener: DownloadListener?) {
            mDownloadListener?.let { SingleHolder.INSTANCE.mDownloadGlobalListener.remove(mDownloadListener.hashCode(), mDownloadListener) }
        }

        fun syncProgressDataToMain(progressData: ProgressData) {
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
                    complete(progressData)
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

        private fun getListener(progressData: ProgressData): DownloadListener? {
            return SingleHolder.INSTANCE.mDdownloadListenerSparseArray.get(progressData.id)
        }

        private fun removeListener(progressData: ProgressData) {
            SingleHolder.INSTANCE.mDdownloadListenerSparseArray.remove(progressData.id)
        }

        private fun pause(progressData: ProgressData) {
            getListener(progressData)?.onPause(progressData.id, progressData.sofar, progressData.total)
                    ?: SingleHolder.INSTANCE.mDownloadGlobalListener.forEach { listener ->
                        listener.value.onPause(progressData.id, progressData.sofar, progressData.total)
                    }
            removeListener(progressData)
            Log.e("vanda", "size = ${SingleHolder.INSTANCE.mDdownloadListenerSparseArray.size()}")
        }

        private fun progress(progressData: ProgressData) {
            getListener(progressData)?.let { progress(it, progressData) }
                    ?: globalProgress(progressData)
        }

        private fun progress(listener: DownloadListener?, progressData: ProgressData) {
            Log.e("vanda", "id = ${progressData.id}, threadId = ${progressData.threadId}, percentChild = ${progressData.percentChild} sofarChild = ${progressData.sofarChild} totalChild = ${progressData.totalChild} sofar = ${progressData.sofar}, total = ${progressData.total} isInit = ${progressData.isInit}")
            listener?.onProgress(
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

        private fun complete(progressData: ProgressData) {
            if (progressData.allComplete && progressData.status == OnStatus.COMPLETE) {
                getListener(progressData)?.let { complete(it, progressData) }
                        ?: globalComplete(progressData)
            }
        }

        private fun complete(listener: DownloadListener?, progressData: ProgressData) {
            if (progressData.allComplete && progressData.status == OnStatus.COMPLETE) {
                listener?.onComplete(progressData.id, progressData.total)
                Log.e("vanda", "size = ${SingleHolder.INSTANCE.mDdownloadListenerSparseArray.size()}")
            }
        }


        private fun globalProgress(progressData: ProgressData) {
            SingleHolder.INSTANCE.mDownloadGlobalListener.forEach { listener ->
                progress(listener.value, progressData)
            }
        }

        private fun globalComplete(progressData: ProgressData) {
            SingleHolder.INSTANCE.mDownloadGlobalListener.forEach { listener ->
                complete(listener.value, progressData)
            }
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