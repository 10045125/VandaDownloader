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

package vanda.wzl.vandadownloader.core

import vanda.wzl.vandadownloader.core.database.RemarkMultiThreadPointSqlEntry
import vanda.wzl.vandadownloader.core.database.RemarkPointSql
import vanda.wzl.vandadownloader.core.database.RemarkPointSqlEntry
import vanda.wzl.vandadownloader.core.database.RemarkPointSqlImpl

abstract class AbstractDownloadTaskSchedule() : ExeProgressCalc {
    private val mRemarkPointSql: RemarkPointSql

    init {
        mRemarkPointSql = RemarkPointSqlImpl()
    }

    override fun remarkPointSqlEntry(id: Int): RemarkPointSqlEntry {
        return mRemarkPointSql.remarkPointSqlEntry(id)
    }

    override fun insert(remarkPointSqlEntry: RemarkPointSqlEntry) {
        mRemarkPointSql.insert(remarkPointSqlEntry)
    }

    override fun update(remarkPointSqlEntry: RemarkPointSqlEntry) {
        mRemarkPointSql.update(remarkPointSqlEntry)
    }

    override fun remarkMultiThreadPointSqlEntry(downloadId: Int, threadId: Long): RemarkMultiThreadPointSqlEntry {
        return mRemarkPointSql.remarkMultiThreadPointSqlEntry(downloadId, threadId)
    }

    override fun update(remarkMultiThreadPointSqlEntry: RemarkMultiThreadPointSqlEntry) {
        mRemarkPointSql.update(remarkMultiThreadPointSqlEntry)
    }

    override fun insert(remarkMultiThreadPointSqlEntry: RemarkMultiThreadPointSqlEntry) {
        mRemarkPointSql.insert(remarkMultiThreadPointSqlEntry)
    }

    override fun findThreadInfo(downloadId: Int): ArrayList<RemarkMultiThreadPointSqlEntry> {
        return mRemarkPointSql.findThreadInfo(downloadId)
    }

    override fun deleteThreadInfo(downloadId: Int) {
        mRemarkPointSql.deleteThreadInfo(downloadId)
    }

    override fun delete(downloadId: Int) {
        mRemarkPointSql.delete(downloadId)
    }
}