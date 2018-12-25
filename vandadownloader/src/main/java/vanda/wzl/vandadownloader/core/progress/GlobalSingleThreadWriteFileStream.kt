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

package vanda.wzl.vandadownloader.core.progress

import android.os.HandlerThread
import android.os.Message

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
