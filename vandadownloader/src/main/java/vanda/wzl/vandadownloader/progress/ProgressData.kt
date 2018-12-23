package vanda.wzl.vandadownloader.progress

import vanda.wzl.vandadownloader.DownloadListener
import vanda.wzl.vandadownloader.ExeProgressCalc
import vanda.wzl.vandadownloader.database.RemarkMultiThreadPointSqlEntry
import vanda.wzl.vandadownloader.database.RemarkPointSqlEntry
import vanda.wzl.vandadownloader.status.OnStatus

class ProgressData {
    private var mNext: ProgressData? = null

    var id: Long = 0
    var url: String = ""
    var path: String = ""
    var sofar: Long = 0
    var sofarChild: Long = 0
    var total: Long = 0
    var totalChild: Long = 0
    var speed: String = "0KB"
    var speedChild: String = "0KB"
    var percent = "0.00"
    var percentChild = "0.00"
    var threadId: Int = 0

    var segment: Long = 0
    var extSize: Long = 0

    @OnStatus
    var status: Int = 0

    var exeProgressCalc: ExeProgressCalc? = null
    var downloadListener: DownloadListener? = null

    private var remarkPointSqlEntry: RemarkPointSqlEntry = RemarkPointSqlEntry()
    private var remarkMultiThreadPointSqlEntry: RemarkMultiThreadPointSqlEntry = RemarkMultiThreadPointSqlEntry()

    fun fillingRemarkPointSqlEntry(): RemarkPointSqlEntry {
        remarkPointSqlEntry.fillingValue(id, url, path, sofar, total, status)
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
        speed = "0KB"
        speedChild = speed
        threadId = 0

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
        private val MAX_POOL_SIZE = 300
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
