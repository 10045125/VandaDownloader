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

package vanda.wzl.vandadownloader.core.database

import android.content.ContentValues
import android.database.Cursor
import android.util.Log
import vanda.wzl.vandadownloader.core.status.OnStatus
import java.lang.Exception

class RemarkMultiThreadPointSqlEntry(var cursor: Cursor?) {

    var invalid = false
    var id: Int = 0
    var url: String = ""
    var total: Long = 0
    var sofar: Long = 0
    var threadId: Int = 0
    var downloadFileId: Int = 0
    var status: Int = vanda.wzl.vandadownloader.core.status.OnStatus.INVALID
    var segment: Long = 0
    var extSize: Long = 0

    init {
        init()
    }

    constructor() : this(null)

    private fun init() {
        if (cursor != null && cursor!!.count == 1) {
            try {
                while (cursor!!.moveToNext()) {
                    this.id = cursor!!.getInt(cursor!!.getColumnIndex(RemarkMultiThreadPointSqlKey.ID))
                    this.status = cursor!!.getInt(cursor!!.getColumnIndex(RemarkMultiThreadPointSqlKey.STATUS))
                    this.threadId = cursor!!.getInt(cursor!!.getColumnIndex(RemarkMultiThreadPointSqlKey.THREAD_ID))
                    this.downloadFileId = cursor!!.getInt(cursor!!.getColumnIndex(RemarkMultiThreadPointSqlKey.DOWNLOADFILE_ID))
                    this.sofar = cursor!!.getLong(cursor!!.getColumnIndex(RemarkMultiThreadPointSqlKey.DOWNLOAD_SOFAR))
                    this.total = cursor!!.getLong(cursor!!.getColumnIndex(RemarkMultiThreadPointSqlKey.DOWNLOAD_LENGTH))
                    this.segment = cursor!!.getLong(cursor!!.getColumnIndex(RemarkMultiThreadPointSqlKey.NORMAL_SIZE))
                    this.extSize = cursor!!.getLong(cursor!!.getColumnIndex(RemarkMultiThreadPointSqlKey.EXT_SIZE))
                    this.url = cursor!!.getString(cursor!!.getColumnIndex(RemarkMultiThreadPointSqlKey.URL))
                    invalid = true
                    break
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fillingValue(id: Int, url: String, sofar: Long, total: Long, status: Int, threadId: Int, downloadId: Int, segment: Long, extSize: Long) {
        this.id = id
        this.url = url
        this.sofar = sofar
        this.total = total
        this.status = status
        this.threadId = threadId
        this.downloadFileId = downloadId
        this.segment = segment
        this.extSize = extSize
        Log.i("vanda", "fillingValue threadId = $threadId segment = $segment sofar = $sofar startPoint = ${threadId * segment + sofar}  extSize = $extSize")
        invalid = true
    }

    fun reset() {
        this.id = 0
        this.url = ""
        this.sofar = -1
        this.total = -1
        this.status = vanda.wzl.vandadownloader.core.status.OnStatus.INVALID
        this.threadId = -1
        this.downloadFileId = -1
        this.segment = -1
        this.extSize = -1
        invalid = false
    }

    fun toContentValues(): ContentValues {
        val cv = ContentValues()
        cv.put(RemarkMultiThreadPointSqlKey.URL, url)
        cv.put(RemarkMultiThreadPointSqlKey.DOWNLOAD_SOFAR, sofar)
        cv.put(RemarkMultiThreadPointSqlKey.DOWNLOAD_LENGTH, total)
        cv.put(RemarkMultiThreadPointSqlKey.STATUS, status)
        cv.put(RemarkMultiThreadPointSqlKey.THREAD_ID, threadId)
        cv.put(RemarkMultiThreadPointSqlKey.DOWNLOADFILE_ID, downloadFileId)
        cv.put(RemarkMultiThreadPointSqlKey.NORMAL_SIZE, segment)
        cv.put(RemarkMultiThreadPointSqlKey.EXT_SIZE, extSize)

        return cv
    }
}