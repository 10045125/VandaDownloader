package vanda.wzl.vandadownloader

interface DownloadListener {
    //Log.d("vanda", "sofar = ${progressData.sofar} segment = ${progressData.segment} totalProgress = $totalProgress  percent = $percent percentChild = $percentChild speed = $speed  threadId = ${progressData.threadId}")
    fun progress(sofar: Long, total: Long, childPercent: String, percent: String, threadId: Int, speed: String)
}