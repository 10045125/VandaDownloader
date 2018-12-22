package vanda.wzl.vandadownloader

interface DownloadListener {
    fun progress(sofar: Long, sofarChild: Long, total: Long, totalChild: Long, percent: String, percentChild: String, speed: String, speedChild: String, threadId: Int)

    fun onComplete()
}