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

import android.os.Handler
import android.os.Looper
import android.os.Message

class HandlerSegment(looper: Looper) : Handler(looper) {

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            MSG_WRITE -> proSegmentBytesToStoreSync(msg)
            MSG_QIUT -> {
            }
        }
    }

    private fun proSegmentBytesToStoreSync(msg: Message) {
////        val array = msg.obj as Array<*>
////        val messageData = array[0] as MessageData
//        val writeSeparation = array[1] as WriteSeparation
        val writeSeparation = msg.obj as WriteSeparation
        try {
            writeSeparation.onWriteSegmentBytesToStore()
            writeSeparation.syncCurData()
        } catch (e: Exception) {
            e.printStackTrace()
        }
//        messageData.recycle()
    }

    companion object {
        internal val MSG_WRITE = 0x11011
        internal val MSG_QIUT = 0x11022
    }

}
