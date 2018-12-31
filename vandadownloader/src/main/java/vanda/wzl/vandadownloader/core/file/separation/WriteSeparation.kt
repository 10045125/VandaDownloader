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

package vanda.wzl.vandadownloader.core.file.separation

import vanda.wzl.vandadownloader.core.DownloadListener
import vanda.wzl.vandadownloader.core.ExeProgressCalc
import vanda.wzl.vandadownloader.core.progress.HandlerProgressToThreadPool
import vanda.wzl.vandadownloader.core.progress.ProgressData


interface WriteSeparation {
    fun onWriteSegmentBytesToStore()
    fun syncCurData()
    fun quarkBufferSink(): quarkokio.BufferedSink
    fun sofar(sofar: Long)
    fun total(total: Long)
    fun status(@vanda.wzl.vandadownloader.core.status.OnStatus status: Int)
    fun id(id: Int)
    fun threadId(id: Int)
    fun exeProgressCalc(exeProgressCalc: ExeProgressCalc)
    fun time(time: Long)
    fun segment(segment: Long)
    fun extSize(extSize: Long)
    fun downloadListener(downloadListener: DownloadListener)
    fun url(url: String)
    fun path(path: String)
    fun supportMultiThread(supportMultiThread: Boolean)

    companion object {
        fun alreadyComplete(sofar: Long, total: Long, segment: Long, id: Int, threadId: Int, url: String, path: String, extSize: Long, supportMultiThread: Boolean, exeProgressCalc: ExeProgressCalc, downloadListener: DownloadListener) {
            val progressData = ProgressData.obtain()
            progressData.sofarChild = sofar
            progressData.total = total
            progressData.totalChild = segment
            progressData.id = id
            progressData.threadId = threadId
            progressData.speedChild = 0
            progressData.status = vanda.wzl.vandadownloader.core.status.OnStatus.COMPLETE
            progressData.exeProgressCalc = exeProgressCalc
            progressData.downloadListener = downloadListener
            progressData.url = url
            progressData.path = path
            progressData.segment = segment
            progressData.extSize = extSize
            progressData.supportMultiThread = supportMultiThread
            HandlerProgressToThreadPool.ayncProgressData(progressData)
        }
    }
}
