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

interface RemarkPointSql {
    fun remarkPointSqlEntry(id: Long): RemarkPointSqlEntry
    fun insert(remarkPointSqlEntry: RemarkPointSqlEntry)
    fun update(remarkPointSqlEntry: RemarkPointSqlEntry)
    fun delete(downloadId: Long)

    fun remarkMultiThreadPointSqlEntry(downloadId: Long, threadId: Long): RemarkMultiThreadPointSqlEntry

    fun update(remarkMultiThreadPointSqlEntry: RemarkMultiThreadPointSqlEntry)

    fun insert(remarkMultiThreadPointSqlEntry: RemarkMultiThreadPointSqlEntry)

    fun findThreadInfo(downloadId: Long): ArrayList<RemarkMultiThreadPointSqlEntry>

    fun deleteThreadInfo(downloadId: Long)
}