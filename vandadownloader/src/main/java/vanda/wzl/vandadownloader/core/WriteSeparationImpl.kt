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

package vanda.wzl.vandadownloader.core

import vanda.wzl.vandadownloader.core.file.separation.WriteSeparation
import vanda.wzl.vandadownloader.core.file.separation.WriteSeparation.Companion.fillProgressData
import java.io.IOException
import java.io.OutputStream
import java.util.*

internal class WriteSeparationImpl(
        private val mTimes: Array<Long>,
        private val mQuarkBufferedSink: quarkokio.BufferedSink,
        private val mSource: quarkokio.Source?,
        private val mOutputStream: OutputStream?,
        private val mQuarkBufferedSinkQueue: Queue<WriteSeparation>,
        private val mQuarkBufferedSinkQueueWait: Queue<WriteSeparation>
) : WriteSeparation {

    private var sofar: Long = -1
    private var total: Long = -1
    private var id: Int = -1
    private var time: Long = 0
    private var threadId: Int = 0
    private var segment: Long = 0
    private var extSize: Long = 0
    private var status: Int = vanda.wzl.vandadownloader.core.status.OnStatus.INVALID
    private var exeProgressCalc: ExeProgressCalc? = null
    private var url: String = ""
    private var path: String = ""
    private var supportMultiThread: Boolean = false

    override fun time(time: Long) {
        this.time = time
    }

    override fun sofar(sofar: Long) {
        this.sofar = sofar
    }

    override fun total(total: Long) {
        this.total = total
    }

    override fun status(status: Int) {
        this.status = status
    }

    override fun id(id: Int) {
        this.id = id
    }

    override fun threadId(id: Int) {
        this.threadId = id
    }

    override fun exeProgressCalc(exeProgressCalc: ExeProgressCalc) {
        this.exeProgressCalc = exeProgressCalc
    }

    override fun segment(segment: Long) {
        this.segment = segment
    }

    override fun url(url: String) {
        this.url = url
    }

    override fun path(path: String) {
        this.path = path
    }

    override fun extSize(extSize: Long) {
        this.extSize = extSize
    }

    override fun supportMultiThread(supportMultiThread: Boolean) {
        this.supportMultiThread = supportMultiThread
    }

    private fun progressIntval(): Int {
        return PROGRESS_INTVAL
    }

    override fun onWriteSegmentBytesToStore() {
        try {
            mQuarkBufferedSink.emit()
            val intval = System.currentTimeMillis() - mTimes[0]
            if (status != vanda.wzl.vandadownloader.core.status.OnStatus.PROGRESS || intval > progressIntval()) {

                val curSofar = exeProgressCalc?.let { exeProgressCalc!!.sofar(threadId) } ?: 0
                val mSpeedIncrement = (curSofar - mTimes[1]) * ONE_SECEND_TIME / (intval + 1)

                mTimes[1] = curSofar
                mTimes[0] = System.currentTimeMillis()

                if (status == vanda.wzl.vandadownloader.core.status.OnStatus.COMPLETE || status == vanda.wzl.vandadownloader.core.status.OnStatus.PAUSE) {
                    mSource?.close()
                    mOutputStream?.close()
                }

                fillProgressData(mSpeedIncrement, sofar, total, segment, id, threadId, url, path, extSize, supportMultiThread, exeProgressCalc, status)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun syncCurData() {
        synchronized(mQuarkBufferedSinkQueueWait) {
            if (mQuarkBufferedSinkQueueWait.contains(this)) {
                mQuarkBufferedSinkQueue.offer(this)
                mQuarkBufferedSinkQueueWait.remove(this)
            }
        }
    }

    override fun quarkBufferSink(): quarkokio.BufferedSink {
        return mQuarkBufferedSink
    }

    companion object {
        private const val PROGRESS_INTVAL = 1000 //ms
        private const val ONE_SECEND_TIME = 1000 //ms
    }
}