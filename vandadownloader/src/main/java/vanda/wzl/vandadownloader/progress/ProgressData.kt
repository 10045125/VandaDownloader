package vanda.wzl.vandadownloader.progress

import vanda.wzl.vandadownloader.DownloadListener
import vanda.wzl.vandadownloader.ExeProgressCalc
import vanda.wzl.vandadownloader.status.OnStatus

class ProgressData {
    private var mNext: ProgressData? = null

    var id: Long = 0
    var sofar: Long = 0
    var total: Long = 0
    var segment: Long = 0
    var threadId: Int = 0
    var totalProgress: Long = 0
    var speed: String = "0KB"
    var percent = "0.00"
    var percentChild = "0.00"

    @OnStatus
    var status: Int = 0

    var exeProgressCalc: ExeProgressCalc? = null
    var downloadListener: DownloadListener? = null

    private fun reset() {
        id = -1
        sofar = -1
        total = -1
        status = -1
        threadId = 0
        segment = 0
        totalProgress = 0
        speed = "0KB"
        percent = "0.00"
        percentChild = "0.00"
        downloadListener = null
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
