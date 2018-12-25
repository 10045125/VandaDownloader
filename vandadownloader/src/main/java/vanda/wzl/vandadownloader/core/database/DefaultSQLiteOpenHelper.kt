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

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DefaultSQLiteOpenHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(CREATE_MULTI_THREAD_TABLE)
        db?.execSQL(CREATE_NORMAL_TABLE)
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     *
     *
     *
     * The SQLite ALTER TABLE documentation can be found
     * [here](http://sqlite.org/lang_altertable.html). If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     *
     *
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     *
     *
     * @param db The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    companion object {
        private const val DATABASE_NAME = "quarkdownloader.db"
        private const val DATABASE_VERSION = 1

        private const val CREATE_MULTI_THREAD_TABLE = "CREATE TABLE IF NOT EXISTS " +
                RemarkMultiThreadPointSqlKey.TABLE_NAME_MULTI_THREAD + "( " +
                RemarkMultiThreadPointSqlKey.ID + " INTEGER PRIMARY KEY, " + // id
                RemarkMultiThreadPointSqlKey.URL + " VARCHAR, " + // mUrl
                RemarkMultiThreadPointSqlKey.DOWNLOAD_LENGTH + " INTEGER, " + // path
                RemarkMultiThreadPointSqlKey.DOWNLOAD_SOFAR + " INTEGER, " +
                RemarkMultiThreadPointSqlKey.NORMAL_SIZE + " INTEGER, " +
                RemarkMultiThreadPointSqlKey.EXT_SIZE + " INTEGER, " +
                RemarkMultiThreadPointSqlKey.THREAD_ID + " INTEGER, " +
                RemarkMultiThreadPointSqlKey.STATUS + " TINYINT(7), " +
                RemarkMultiThreadPointSqlKey.DOWNLOADFILE_ID + " INTEGER " +
                ")"

        private const val CREATE_NORMAL_TABLE = "CREATE TABLE IF NOT EXISTS " +
                RemarkPointSqlKey.TABLE_NAME + "( " +
                RemarkPointSqlKey.ID + " INTEGER PRIMARY KEY, " + // id
                RemarkPointSqlKey.URL + " VARCHAR, " + // mUrl
                RemarkPointSqlKey.PATH + " VARCHAR, " + // path
                RemarkPointSqlKey.STATUS + " TINYINT(7), " + // status ,ps SQLite will auto change to integer.
                RemarkPointSqlKey.SOFAR + " INTEGER, " +// so far
                RemarkPointSqlKey.TOTAL + " INTEGER, " +// total
                RemarkPointSqlKey.ERR_MSG + " VARCHAR, " + // error message
                RemarkPointSqlKey.ETAG + " VARCHAR, " + // e tag
                RemarkPointSqlKey.FILENAME + " VARCHAR, " + // path as directory
                RemarkPointSqlKey.POSTBODY + " VARCHAR, " +// post body
                RemarkPointSqlKey.FILECONTIUE + "  TINYINT(1) DEFAULT 0, " +// file continue download
                RemarkPointSqlKey.IS_NEED_REFER + "  TINYINT(1) DEFAULT 1, " +// file continue download
                RemarkPointSqlKey.UPDATE_URL + " VARCHAR " +// update url
                ")"
    }
}