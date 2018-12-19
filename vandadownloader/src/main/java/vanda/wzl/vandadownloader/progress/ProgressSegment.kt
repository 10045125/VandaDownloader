package vanda.wzl.vandadownloader.progress


interface ProgressSegment {
    fun onStart(id: Long, sofar: Long, total: Long)
    fun onProgress(id: Long, sofar: Long, total: Long)
    fun onComplete(id: Long, sofar: Long, total: Long)
    fun onError(id: Long, sofar: Long, total: Long)
    fun onTry(id: Long, sofar: Long, total: Long)
}
