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

import vanda.wzl.vandadownloader.core.DownloadListener

interface TaskDispatherAttribute {
    fun start(url: String, listener: DownloadListener, threadNum: Int, path: String, pathAsDirectory: Boolean, callbackProgressTimes: Int, callbackProgressMinIntervalMillis: Int, autoRetryTimes: Int, forceReDownload: Boolean, header: Map<String, String>, isWifiRequired: Boolean, isGroup: Boolean, postBody: String, fileSize: Long, updateUrl: String)
    fun pause(downloadId: Int)
    fun isDownloading(downloadId: Int): Boolean
    fun isDownloading(url: String, path: String): Boolean
    fun getStatus(downloadId: Int): Int
    fun getSofar(downloadId: Int): Long
    fun getTotal(downloadId: Int): Long
    fun isIdle(): Boolean
    fun isBreakPointContinued(downloadId: Int): Boolean
}