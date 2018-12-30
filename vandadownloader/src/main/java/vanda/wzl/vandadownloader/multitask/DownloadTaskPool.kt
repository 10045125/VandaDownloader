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

package vanda.wzl.vandadownloader.multitask

import android.util.SparseArray
import vanda.wzl.vandadownloader.core.DownloadTaskAttribute
import vanda.wzl.vandadownloader.core.DownloadTaskStatus

class DownloadTaskPool(maxTaskNum: Int) : DownloadTaskStatus {

    companion object {
        internal const val MAX_TASK_NUM = 3
    }

    private var mMaxTaskNum = MAX_TASK_NUM

    init {
        mMaxTaskNum = maxTaskNum
    }

    private val downloadingTaskSparseArray: SparseArray<DownloadTaskAttribute>
        get() = SparseArray()
    private val downloadTaskWaitingSparseArray: SparseArray<DownloadTaskAttribute>
        get() = SparseArray()


    override fun complete(downloadId: Int) {
        synchronized(downloadingTaskSparseArray) {
            downloadingTaskSparseArray.remove(downloadId)
            if (downloadTaskWaitingSparseArray.size() > 0 && downloadingTaskSparseArray.size() < mMaxTaskNum) {
                val fileDownloadTask = downloadTaskWaitingSparseArray.valueAt(0)
                downloadTaskWaitingSparseArray.removeAt(0)
                downloadingTaskSparseArray.put(fileDownloadTask.getTaskId(), fileDownloadTask)
                fileDownloadTask.start()
            }
        }
    }

    override fun pauseComplete(downloadId: Int) {

    }

    fun execTask(fileDownloadTask: DownloadTaskAttribute) {
        synchronized(downloadingTaskSparseArray) {
            if (downloadingTaskSparseArray.size() == mMaxTaskNum) {
                downloadTaskWaitingSparseArray.put(fileDownloadTask.getTaskId(), fileDownloadTask)
                fileDownloadTask.pendding()
            } else {
                downloadingTaskSparseArray.put(fileDownloadTask.getTaskId(), fileDownloadTask)
                fileDownloadTask.pendding()
                fileDownloadTask.start()
            }
        }
    }

    fun isInTaskPool(downloadId: Int): Boolean {
        synchronized(downloadingTaskSparseArray) {
            val fileDownloadTask = downloadingTaskSparseArray.get(downloadId)
            return fileDownloadTask != null
        }
    }

    fun isIdle(): Boolean {
        synchronized(downloadingTaskSparseArray) {
            return downloadingTaskSparseArray.size() <= 0
        }
    }

    fun pause(id: Int): Boolean {
        synchronized(downloadingTaskSparseArray) {
            val fileDownloadTask = downloadingTaskSparseArray.get(id)
            if (fileDownloadTask != null) {
                fileDownloadTask!!.pause()
            }

            val downloadTask = downloadTaskWaitingSparseArray.get(id)
            if (downloadTask != null) {
                downloadTaskWaitingSparseArray.remove(id)
                downloadTask!!.pause()
            }
            complete(id)
            return true
        }
    }

    /**
     * Pause all running task
     */
    fun pauseAll() {


        for (j in 0 until downloadTaskWaitingSparseArray.size()) {
            downloadTaskWaitingSparseArray.valueAt(j).pause()
        }

        downloadTaskWaitingSparseArray.clear()


        val ids = IntArray(downloadingTaskSparseArray.size())
        for (i in 0 until downloadingTaskSparseArray.size()) {
            ids[i] = downloadingTaskSparseArray.keyAt(i)
        }

        for (id in ids) {
            pause(id)
        }
    }

    fun getAllExactDownloadIds(): IntArray {
        synchronized(downloadingTaskSparseArray) {
            val list = IntArray(downloadingTaskSparseArray.size())
            for (i in 0 until downloadingTaskSparseArray.size()) {
                list[i] = downloadingTaskSparseArray.keyAt(i)
            }
            return list
        }
    }
}