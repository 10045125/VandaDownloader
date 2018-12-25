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

package vanda.wzl.vandadownloader.core.file.io

import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile

class RandomAcessFileOutputStream : OutputStream() {

    private var mRandomAccessFile: RandomAccessFile? = null

    fun updateOutputStream(randomAccessFile: RandomAccessFile, seek: Long): RandomAcessFileOutputStream {
        mRandomAccessFile = randomAccessFile
        if (seek > 0) {
            mRandomAccessFile!!.seek(seek)
        }
        return this
    }

    fun filePointer(): Long {
        return mRandomAccessFile!!.filePointer
    }

    @Throws(IOException::class)
    override fun write(b: Int) {
        write(byteArrayOf(b.toByte()), 0, 1)
    }

    @Throws(IOException::class)
    override fun write(b: ByteArray, off: Int, len: Int) {
        mRandomAccessFile!!.write(b, off, len)
    }

    @Throws(IOException::class)
    override fun close() {
        mRandomAccessFile!!.close()
    }
}
