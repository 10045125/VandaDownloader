package vanda.wzl.vandadownloader.io.file.separation

import vanda.wzl.vandadownloader.DownloadListener
import vanda.wzl.vandadownloader.ExeProgressCalc
import vanda.wzl.vandadownloader.progress.GlobalSingleThreadHandlerProgress
import vanda.wzl.vandadownloader.progress.ProgressData
import vanda.wzl.vandadownloader.status.OnStatus
import vanda.wzl.vandadownloader.util.SpeedUtils


interface WriteSeparation {
    fun onWriteSegmentBytesToStore()
    fun syncCurData()
    fun quarkBufferSink(): quarkokio.BufferedSink
    fun sofar(sofar: Long)
    fun total(total: Long)
    fun status(@OnStatus status: Int)
    fun id(id: Long)
    fun threadId(id: Int)
    fun exeProgressCalc(exeProgressCalc: ExeProgressCalc)
    fun time(time: Long)
    fun segment(segment: Long)
    fun extSize(extSize: Long)
    fun downloadListener(downloadListener: DownloadListener)
    fun url(url: String)
    fun path(path: String)
    fun supportMultiThread(supportMultiThread: Boolean)

    companion object {
        fun alreadyComplete(sofar: Long, total: Long, segment: Long, id: Long, threadId: Int, url: String, path: String, extSize: Long, supportMultiThread: Boolean, exeProgressCalc: ExeProgressCalc, downloadListener: DownloadListener) {
            val progressData = ProgressData.obtain()
            progressData.sofarChild = sofar
            progressData.total = total
            progressData.totalChild = segment
            progressData.id = id
            progressData.threadId = threadId
            progressData.speedChild = SpeedUtils.formatSize(0)
            progressData.status = OnStatus.COMPLETE
            progressData.exeProgressCalc = exeProgressCalc
            progressData.downloadListener = downloadListener
            progressData.url = url
            progressData.path = path
            progressData.segment = segment
            progressData.extSize = extSize
            progressData.supportMultiThread = supportMultiThread
            GlobalSingleThreadHandlerProgress.ayncProgressData(progressData)
        }
    }
}
