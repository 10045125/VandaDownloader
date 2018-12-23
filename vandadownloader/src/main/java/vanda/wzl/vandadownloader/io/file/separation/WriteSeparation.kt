package vanda.wzl.vandadownloader.io.file.separation

import vanda.wzl.vandadownloader.DownloadListener
import vanda.wzl.vandadownloader.ExeProgressCalc
import vanda.wzl.vandadownloader.status.OnStatus


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

}
