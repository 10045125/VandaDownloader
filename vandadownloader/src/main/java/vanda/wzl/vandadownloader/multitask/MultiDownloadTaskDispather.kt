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

import vanda.wzl.vandadownloader.core.DownloadTaskSchedule
import vanda.wzl.vandadownloader.core.database.RemarkPointSqlImpl
import vanda.wzl.vandadownloader.core.util.DownloadUtils
import java.io.File

class MultiDownloadTaskDispather : TaskDispatherAttribute {

    private var mDownloadTaskPool: DownloadTaskPool = DownloadTaskPool(3)
    private val mRemarkPointSqlImpl = RemarkPointSqlImpl()

    private var mDownloadTaskSchedule: DownloadTaskSchedule? = null

    override fun start(url: String, threadNum: Int, path: String, callbackProgressTimes: Int, callbackProgressMinIntervalMillis: Int, autoRetryTimes: Int, forceReDownload: Boolean, header: Map<String, String>, isWifiRequired: Boolean,  postBody: String) {
        if (!isDownloading(url, path)) {
            mDownloadTaskSchedule = DownloadTaskSchedule(threadNum, mDownloadTaskPool, url, path)
            mDownloadTaskPool.execTask(mDownloadTaskSchedule!!)
        }
    }

    override fun pause(downloadId: Int) {
        mDownloadTaskPool.pause(downloadId)
    }

    override fun isDownloading(downloadId: Int): Boolean {
        return mDownloadTaskPool.isInTaskPool(downloadId)
    }

    override fun isDownloading(url: String, path: String): Boolean {
        return isDownloading(DownloadUtils.generateId(url, path))
    }

    override fun getStatus(downloadId: Int): Int {
       return mRemarkPointSqlImpl.remarkPointSqlEntry(downloadId).status
    }

    override fun getSofar(downloadId: Int): Long {
        return mRemarkPointSqlImpl.remarkPointSqlEntry(downloadId).sofar
    }

    override fun getTotal(downloadId: Int): Long {
        return mRemarkPointSqlImpl.remarkPointSqlEntry(downloadId).total
    }

    override fun isIdle(): Boolean {
        return mDownloadTaskPool.isIdle()
    }

    override fun isBreakPointContinued(downloadId: Int): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    fun clean(downloadId: Int) {
        mRemarkPointSqlImpl.delete(downloadId)
        mRemarkPointSqlImpl.deleteThreadInfo(downloadId)

    }

    fun deletefile() {
        val list = mRemarkPointSqlImpl.remarkPointSqlEntrys()
        for (remarkPointEntry in list) {
            val file = File(remarkPointEntry.path)
            if (file.exists()) {
                file.delete()
            }
        }
    }

}