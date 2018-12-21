package vanda.wzl.vandadownloader.progress

import android.os.HandlerThread
import android.os.Message
import vanda.wzl.vandadownloader.io.file.separation.HandlerSegment

class GlobalSingleThreadHandlerProgress private constructor() {

    private val mHandlerThread: HandlerThread = HandlerThread("GlobalSingleThreadHandlerProgress")
    private val mHandlerSegment: HandlerProgress

    private object SingleHolder {
        internal val INSTANCE = GlobalSingleThreadHandlerProgress()
    }

    init {
        mHandlerThread.start()
        mHandlerSegment = HandlerProgress(mHandlerThread.looper)
    }

    companion object {
        fun ayncProgressData(progressData: ProgressData) {
            val msg = Message.obtain()
            msg.what = HandlerProgress.MSG_WRITE
            msg.obj = progressData
            SingleHolder.INSTANCE.mHandlerSegment.sendMessage(msg)
        }
    }
}
