package vanda.wzl.vandadownloader.io.file.separation

import okio.BufferedSink
import vanda.wzl.vandadownloader.status.OnStatus


interface WriteSeparation {
    fun onWriteSegmentBytesToStore()
    fun syncCurData()
    fun quarkBufferSink(): BufferedSink
    fun sofar(sofar: Long)
    fun total(total: Long)
    fun status(@OnStatus status: Int)
    fun id(id: Long)
    fun time(time: Long)
}
