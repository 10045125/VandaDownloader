package vanda.wzl.vandadownloader.database

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