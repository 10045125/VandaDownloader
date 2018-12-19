package vanda.wzl.vandadownloader.io.file.io

import java.io.IOException
import java.io.OutputStream
import java.io.RandomAccessFile

/**
 * Copyright (C) 2005 - 2016 UCWeb Inc. All Rights Reserved.
 * Description :
 *
 *
 * Creation    : 2017/6/28
 * Author      : zhonglian.wzl@alibaba-inc.com
 *
 * @author wzl_vanda
 * @date 2017/06/28
 */

class RandomAcessFileOutputStream(randomAccessFile: RandomAccessFile) : OutputStream() {

    private var mRandomAccessFile: RandomAccessFile? = null

    init {
        updateOutputStream(randomAccessFile)
    }

    fun updateOutputStream(randomAccessFile: RandomAccessFile): RandomAcessFileOutputStream {
        mRandomAccessFile = randomAccessFile
        return this
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
