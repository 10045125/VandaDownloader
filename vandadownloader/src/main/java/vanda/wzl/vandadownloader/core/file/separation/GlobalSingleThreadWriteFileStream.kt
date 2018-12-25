/*
 * Copyright (c) 2018 YY Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package vanda.wzl.vandadownloader.core.file.separation

import android.os.HandlerThread
import android.os.Message

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
    }
}
