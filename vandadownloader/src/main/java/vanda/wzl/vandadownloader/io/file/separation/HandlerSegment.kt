package vanda.wzl.vandadownloader.io.file.separation


import android.os.Handler
import android.os.Looper
import android.os.Message
import vanda.wzl.vandadownloader.progress.ProgressSegment

class HandlerSegment(looper: Looper) : Handler(looper) {

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            MSG_WRITE -> proSegmentBytesToStoreSync(msg)
            MSG_QIUT -> {
            }
        }
    }

    private fun proSegmentBytesToStoreSync(msg: Message) {
        val writeSeparation = msg.obj as WriteSeparation
        try {
            writeSeparation.onWriteSegmentBytesToStore()
            writeSeparation.syncCurData()
        } catch (e: Exception) {
            e.printStackTrace()
            if (writeSeparation == null) {
                throw RuntimeException("WriteSeparation is null !!!")
            }
        }

    }

    companion object {
        internal val MSG_WRITE = 0x1101
        internal val MSG_QIUT = 0x1102
    }

}
