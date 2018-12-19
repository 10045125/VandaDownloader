package vanda.wzl.vandadownloader.io.file.separation

import android.os.HandlerThread
import android.os.Message
import vanda.wzl.vandadownloader.progress.ProgressSegment

class GlobalSingleThreadWriteFileStream private constructor() {

    private val mHandlerThread: HandlerThread = HandlerThread("GlobalSingleThreadWriteFileStream")
    private val mHandlerSegment: HandlerSegment

    private object SingleHolder {
        internal val INSTANCE = GlobalSingleThreadWriteFileStream()
    }

    init {
        mHandlerThread.start()
        mHandlerSegment = HandlerSegment(mHandlerThread.looper)
    }

    companion object {
        fun ayncWrite(writeSeparation: WriteSeparation) {
            val msg = Message.obtain()
            msg.what = HandlerSegment.MSG_WRITE
            msg.obj = writeSeparation
            SingleHolder.INSTANCE.mHandlerSegment.sendMessage(msg)
        }

        fun cancel(writeSeparation: ProgressSegment) {
            SingleHolder.INSTANCE.mHandlerSegment.removeMessages(HandlerSegment.MSG_WRITE, writeSeparation)
        }
    }
}
