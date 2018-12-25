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

import vanda.wzl.vandadownloader.core.DownloadListener
import vanda.wzl.vandadownloader.core.ExeProgressCalc
import vanda.wzl.vandadownloader.core.database.RemarkMultiThreadPointSqlEntry
import vanda.wzl.vandadownloader.core.database.RemarkPointSqlEntry
import vanda.wzl.vandadownloader.core.status.OnStatus

class ProgressData {
    private var mNext: ProgressData? = null

    var id: Long = 0
    var url: String = ""
    var path: String = ""
    var sofar: Long = 0
    var sofarChild: Long = 0
    var total: Long = 0
    var totalChild: Long = 0
    var speed: Long = 0
    var speedChild: Long = 0
    var percent = "0.00"
    var percentChild = "0.00"
    var threadId: Int = 0
    var supportMultiThread = false

    var segment: Long = 0
    var extSize: Long = 0

    var allComplete: Boolean = false

    @vanda.wzl.vandadownloader.core.status.OnStatus
    var status: Int = 0

    var exeProgressCalc: ExeProgressCalc? = null
    var downloadListener: DownloadListener? = null

    private var remarkPointSqlEntry: RemarkPointSqlEntry = RemarkPointSqlEntry()
    private var remarkMultiThreadPointSqlEntry: RemarkMultiThreadPointSqlEntry = RemarkMultiThreadPointSqlEntry()

    fun fillingRemarkPointSqlEntry(): RemarkPointSqlEntry {
        remarkPointSqlEntry.fillingValue(id, url, path, sofar, total, status, supportMultiThread)
        return remarkPointSqlEntry
    }

    fun fillingRemarkMultiThreadPointSqlEntry(): RemarkMultiThreadPointSqlEntry {
        remarkMultiThreadPointSqlEntry.fillingValue(-1, url, sofarChild, total, status, threadId, id, segment, extSize)
        return remarkMultiThreadPointSqlEntry
    }

    private fun reset() {
        id = -1

        sofar = 0
        sofarChild = -1
        total = -1
        totalChild = 0
        percent = "0.00"
        percentChild = "0.00"
        speed = 0
        speedChild = 0
        threadId = 0
        segment = 0
        extSize = 0
        allComplete = false
        supportMultiThread = false

        status = -1
        downloadListener = null

        remarkPointSqlEntry.reset()
        remarkMultiThreadPointSqlEntry.reset()
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

    companion object {
        private const val MAX_POOL_SIZE = 300
        private val sPoolSync = Any()
        private var sPoolSize = 0
        private var sPool: ProgressData? = null

        fun obtain(): ProgressData {
            synchronized(sPoolSync) {
                if (sPool != null) {
                    val m = sPool
                    sPool = m!!.mNext
                    m.mNext = null
                    sPoolSize--
                    m.reset()
                    return m
                }
            }
            return ProgressData()
        }
    }

}
