package vanda.wzl.vandadownloader.database

import android.content.ContentValues
import android.database.Cursor
import vanda.wzl.vandadownloader.status.OnStatus
import java.lang.Exception

class RemarkPointSqlEntry(var cursor: Cursor?) {

    var id: Long? = 0
    var url: String = ""
    var path: String = ""
    var sofar: Long = 0
    var total: Long = 0
    var status: Int = OnStatus.INVALID
    var supportMultiThread = 0 // 默认不支持多线程
    var invalid = false

    init {
        init()
    }

    constructor() : this(null)

    private fun init() {
        if (cursor != null && cursor!!.count == 1) {
            try {
                while (cursor!!.moveToNext()) {
                    this.id = cursor!!.getLong(cursor!!.getColumnIndex(RemarkPointSqlKey.ID))
                    this.sofar = cursor!!.getLong(cursor!!.getColumnIndex(RemarkPointSqlKey.SOFAR))
                    this.total = cursor!!.getLong(cursor!!.getColumnIndex(RemarkPointSqlKey.TOTAL))
                    this.url = cursor!!.getString(cursor!!.getColumnIndex(RemarkPointSqlKey.URL))
                    this.path = cursor!!.getString(cursor!!.getColumnIndex(RemarkPointSqlKey.PATH))
                    this.supportMultiThread = cursor!!.getInt(cursor!!.getColumnIndex(RemarkPointSqlKey.FILECONTIUE))
                    invalid = true
                    break
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fillingValue(id: Long, url: String, path: String, sofar: Long, total: Long, status: Int, supportMultiThread: Boolean): RemarkPointSqlEntry {
        this.id = id
        this.url = url
        this.path = path
        this.sofar = sofar
        this.total = total
        this.status = status
        this.supportMultiThread = if (supportMultiThread) 1 else 0
        invalid = true
        return this
    }

    fun supportMultiThread(): Boolean {
        return supportMultiThread == 1
    }

    fun reset() {
        this.id = 0
        this.url = ""
        this.path = ""
        this.sofar = 0
        this.total = 0
        this.status = OnStatus.INVALID
        this.supportMultiThread = 0
        invalid = false
    }

    fun toContentValues(): ContentValues {
        val cv = ContentValues()
        cv.put(RemarkPointSqlKey.ID, id)
        cv.put(RemarkPointSqlKey.SOFAR, sofar)
        cv.put(RemarkPointSqlKey.TOTAL, total)
        cv.put(RemarkPointSqlKey.STATUS, status)
        cv.put(RemarkPointSqlKey.URL, url)
        cv.put(RemarkPointSqlKey.PATH, path)
        cv.put(RemarkPointSqlKey.FILECONTIUE, supportMultiThread)

        return cv
    }
}