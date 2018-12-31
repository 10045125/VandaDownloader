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

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import vanda.wzl.vandadownloader.core.database.RemarkMultiThreadPointSqlKey.DOWNLOADFILE_ID
import vanda.wzl.vandadownloader.core.database.RemarkMultiThreadPointSqlKey.TABLE_NAME_MULTI_THREAD

class RemarkPointSqlImpl : RemarkPointSql {

    private val db: SQLiteDatabase = DefaultSQLiteOpenHelper.getSQLiteDatabase()

    override fun remarkPointSqlEntrys(): ArrayList<RemarkPointSqlEntry> {
        val cursor = db.rawQuery("SELECT * FROM ${RemarkPointSqlKey.TABLE_NAME}", null)
        val list = ArrayList<RemarkPointSqlEntry>()
        while (cursor != null && cursor.moveToNext()) {
            list.add(RemarkPointSqlEntry().fillingValue(
                    cursor.getInt(cursor.getColumnIndex(RemarkPointSqlKey.ID)),
                    cursor.getString(cursor.getColumnIndex(RemarkPointSqlKey.URL)),
                    cursor.getString(cursor.getColumnIndex(RemarkPointSqlKey.PATH)),
                    cursor.getLong(cursor.getColumnIndex(RemarkPointSqlKey.SOFAR)),
                    cursor.getLong(cursor.getColumnIndex(RemarkPointSqlKey.TOTAL)),
                    cursor.getInt(cursor.getColumnIndex(RemarkPointSqlKey.STATUS)),
                    cursor.getInt(cursor.getColumnIndex(RemarkPointSqlKey.FILECONTIUE)) == 1
            ))
        }
        cursor?.close()
        return list
    }

    override fun remarkPointSqlEntry(id: Int): RemarkPointSqlEntry {
        val cursor = db.rawQuery("select * from " + RemarkPointSqlKey.TABLE_NAME + " where " + RemarkPointSqlKey.ID + "=?", arrayOf(id.toString()))
        return RemarkPointSqlEntry(cursor)
    }

    override fun update(remarkPointSqlEntry: RemarkPointSqlEntry) {
        db.update(RemarkPointSqlKey.TABLE_NAME, remarkPointSqlEntry.toContentValues(), RemarkPointSqlKey.ID + " = ?", arrayOf(remarkPointSqlEntry.id.toString()))
    }

    override fun insert(remarkPointSqlEntry: RemarkPointSqlEntry) {
        db.insert(RemarkPointSqlKey.TABLE_NAME, null, remarkPointSqlEntry.toContentValues())
    }

    override fun remarkMultiThreadPointSqlEntry(downloadId: Int, threadId: Long): RemarkMultiThreadPointSqlEntry {
        val cursor = db.rawQuery(
                "select * from " + RemarkMultiThreadPointSqlKey.TABLE_NAME_MULTI_THREAD +
                        " where " + RemarkMultiThreadPointSqlKey.DOWNLOADFILE_ID + "=? and " +
                        RemarkMultiThreadPointSqlKey.THREAD_ID + "=? ",
                arrayOf(downloadId.toString(), threadId.toString()))
        return RemarkMultiThreadPointSqlEntry(cursor)
    }

    override fun update(remarkMultiThreadPointSqlEntry: RemarkMultiThreadPointSqlEntry) {
        db.update(RemarkMultiThreadPointSqlKey.TABLE_NAME_MULTI_THREAD, remarkMultiThreadPointSqlEntry.toContentValues(), RemarkMultiThreadPointSqlKey.THREAD_ID + " = ? and " + RemarkMultiThreadPointSqlKey.DOWNLOADFILE_ID + " = ?", arrayOf(remarkMultiThreadPointSqlEntry.threadId.toString(), remarkMultiThreadPointSqlEntry.downloadFileId.toString()))
    }

    override fun insert(remarkMultiThreadPointSqlEntry: RemarkMultiThreadPointSqlEntry) {
        db.insert(RemarkMultiThreadPointSqlKey.TABLE_NAME_MULTI_THREAD, null, remarkMultiThreadPointSqlEntry.toContentValues())
    }

    override fun findThreadInfo(downloadId: Int): ArrayList<RemarkMultiThreadPointSqlEntry> {

        var cursor: Cursor? = null
        val list = ArrayList<RemarkMultiThreadPointSqlEntry>()

        try {

            cursor = db.rawQuery("select * from " + RemarkMultiThreadPointSqlKey.TABLE_NAME_MULTI_THREAD + " where " + RemarkMultiThreadPointSqlKey.DOWNLOADFILE_ID + "=?", arrayOf(downloadId.toString()))

            while (cursor != null && cursor.moveToNext()) {
                val remarkMultiThreadPointSqlEntry = RemarkMultiThreadPointSqlEntry()
                remarkMultiThreadPointSqlEntry.fillingValue(
                        cursor.getInt(cursor.getColumnIndex(RemarkMultiThreadPointSqlKey.ID)),
                        cursor.getString(cursor.getColumnIndex(RemarkMultiThreadPointSqlKey.URL)),
                        cursor.getLong(cursor.getColumnIndex(RemarkMultiThreadPointSqlKey.DOWNLOAD_SOFAR)),
                        cursor.getLong(cursor.getColumnIndex(RemarkMultiThreadPointSqlKey.DOWNLOAD_LENGTH)),
                        cursor.getInt(cursor.getColumnIndex(RemarkMultiThreadPointSqlKey.STATUS)),
                        cursor.getInt(cursor.getColumnIndex(RemarkMultiThreadPointSqlKey.THREAD_ID)),
                        cursor.getInt(cursor.getColumnIndex(RemarkMultiThreadPointSqlKey.DOWNLOADFILE_ID)),
                        cursor.getLong(cursor.getColumnIndex(RemarkMultiThreadPointSqlKey.NORMAL_SIZE)),
                        cursor.getLong(cursor.getColumnIndex(RemarkMultiThreadPointSqlKey.EXT_SIZE))
                )
                list.add(remarkMultiThreadPointSqlEntry)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }

        return list
    }

    override fun delete(downloadId: Int) {
        try {
            db.delete(RemarkPointSqlKey.TABLE_NAME, "${RemarkPointSqlKey.ID} = ?", arrayOf(downloadId.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun deleteThreadInfo(downloadId: Int) {
        try {
            db.delete(TABLE_NAME_MULTI_THREAD, "$DOWNLOADFILE_ID = ?", arrayOf(downloadId.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}