package vanda.wzl.vandadownloader

import android.content.Context
import vanda.wzl.vandadownloader.database.RemarkMultiThreadPointSqlEntry
import vanda.wzl.vandadownloader.database.RemarkPointSql
import vanda.wzl.vandadownloader.database.RemarkPointSqlEntry
import vanda.wzl.vandadownloader.database.RemarkPointSqlImpl

abstract class AbstractDownloadTaskSchedule(context: Context) : ExeProgressCalc {
    private val mRemarkPointSql: RemarkPointSql

    init {
        mRemarkPointSql = RemarkPointSqlImpl(context)
    }

    override fun remarkPointSqlEntry(id: Long): RemarkPointSqlEntry {
        return mRemarkPointSql.remarkPointSqlEntry(id)
    }

    override fun insert(remarkPointSqlEntry: RemarkPointSqlEntry) {
        mRemarkPointSql.insert(remarkPointSqlEntry)
    }

    override fun update(remarkPointSqlEntry: RemarkPointSqlEntry) {
        mRemarkPointSql.update(remarkPointSqlEntry)
    }

    override fun remarkMultiThreadPointSqlEntry(downloadId: Long, threadId: Long): RemarkMultiThreadPointSqlEntry {
        return mRemarkPointSql.remarkMultiThreadPointSqlEntry(downloadId, threadId)
    }

    override fun update(remarkMultiThreadPointSqlEntry: RemarkMultiThreadPointSqlEntry) {
        mRemarkPointSql.update(remarkMultiThreadPointSqlEntry)
    }

    override fun insert(remarkMultiThreadPointSqlEntry: RemarkMultiThreadPointSqlEntry) {
        mRemarkPointSql.insert(remarkMultiThreadPointSqlEntry)
    }

    override fun findThreadInfo(downloadId: Long): ArrayList<RemarkMultiThreadPointSqlEntry> {
        return mRemarkPointSql.findThreadInfo(downloadId)
    }

    override fun deleteThreadInfo(downloadId: Long) {
        mRemarkPointSql.deleteThreadInfo(downloadId)
    }
}