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

import android.util.Log
import vanda.wzl.vandadownloader.MainHandler
import vanda.wzl.vandadownloader.core.database.RemarkPointSqlEntry
import vanda.wzl.vandadownloader.core.net.ProviderNetFileTypeImpl
import vanda.wzl.vandadownloader.core.progress.GlobalSingleThreadHandleProgressData
import vanda.wzl.vandadownloader.core.progress.ProgressData
import vanda.wzl.vandadownloader.core.status.OnStatus
import vanda.wzl.vandadownloader.core.threadpool.AutoAdjustThreadPool
import vanda.wzl.vandadownloader.core.util.DownloadUtils
import java.io.File
import java.io.InputStream
import java.util.concurrent.ConcurrentHashMap

class DownloadTaskSchedule(threadNum: Int, private val mDownloadTaskStatus: DownloadTaskStatus, url: String, path: String) : AbstractDownloadTaskSchedule(), DownloadTaskAttribute {

    companion object {
        const val SEEK_SIZE = 3
        const val TYPE_CHUNKED = "chunked"
        const val SPEED_TIME_INTVAL = 1500
        const val PROCESS_TIME_INTVAL = 70
        const val ONE_SECEND_TIME = 1000 //ms
    }

    private var mThreadNum = threadNum
    private val mList = ConcurrentHashMap<Int, ExeRunnable>()
    private var mFileSize: Long = -1
    private var mIsSupportMulti: Boolean = false
    private var mCurSofar = 0L
    private var mSpeedIncrement = 0L
    private var mTime = System.currentTimeMillis()
    private var mPath: String = ""
    private var mDownloadId: Int = -1
    private var mUrl: String = ""

    init {
        mUrl = url
        mPath = path
        mDownloadId = DownloadUtils.generateId(url, mPath)
    }


    override fun getTaskId(): Int {
        return mDownloadId
    }

    override fun pendding() {
    }

    override fun start() {
        AutoAdjustThreadPool.execute(Runnable {
            startAync(mUrl)
        })
    }

    override fun pause() {
        Log.i("vanda", "pause id = $mDownloadId")
        for (r in mList) {
            r.value.pause()
        }
    }

    override fun getSofar(): Long {
        return 0
    }

    override fun getTotal(): Long {
        return 0
    }

    override fun getStatus(): Int {
        return OnStatus.INVALID
    }

    private fun handlerTaskParam(url: String): InputStream {
        val mProviderNetFileType = ProviderNetFileTypeImpl(url)
        val inputStream = mProviderNetFileType.firstIntactInputStream()
        mFileSize = mProviderNetFileType.fileSize()
        mIsSupportMulti = mProviderNetFileType.isSupportMulti()
        return inputStream
    }

    private fun createFile() {
        val file = File(mPath)
        if (!file.exists()) {
            file.createNewFile()
        }
    }

    private fun initDatabaseInfo() {
        if (!remarkPointSqlEntry(mDownloadId).invalid) {
            val remarkPointSqlEntry = RemarkPointSqlEntry()
            remarkPointSqlEntry.fillingValue(mDownloadId, mUrl, mPath, 0, mFileSize, vanda.wzl.vandadownloader.core.status.OnStatus.INVALID, mIsSupportMulti)
            insert(remarkPointSqlEntry)
        }
    }

    private fun exeTask(threadNum: Int, url: String, sofar: Long, segmentSize: Long, exSize: Long, inputStream: InputStream) {
        val thread = threadNum - 1
        for (i in 0..thread) {

            val downloadRunnable = DownloadRunnable(
                    url,
                    segmentSize,
                    exSize,
                    sofar,
                    if (i == 0) inputStream else null,
                    i,
                    mFileSize,
                    mThreadNum,
                    mIsSupportMulti,
                    this,
                    mPath,
                    mDownloadId)

            mList[i] = downloadRunnable
        }

        for (downloadRunnable in mList) {
            AutoAdjustThreadPool.execute(downloadRunnable.value)
        }
    }

    private fun startAync(url: String) {

        mList.clear()
        createFile()
        initDatabaseInfo()
        val threadInfos = findThreadInfo(mDownloadId)
        Log.i("vanda", "url = $url path = $mPath threadInfos = ${threadInfos.size}")
        if (threadInfos.size == 0) {
            val inputStream = handlerTaskParam(url)
            update(RemarkPointSqlEntry().fillingValue(mDownloadId, url, mPath, 0, mFileSize, vanda.wzl.vandadownloader.core.status.OnStatus.INVALID, mIsSupportMulti))
            val threadNum = if (mIsSupportMulti) mThreadNum else 1
            val exSize = mFileSize % threadNum
            val segmentSize = (mFileSize - exSize) / threadNum

            Log.i("vanda", "mFileSize = $mFileSize exSize = $exSize  segmentSize = $segmentSize")

            exeTask(threadNum, url, 0, segmentSize, exSize, inputStream)
        } else {
            mThreadNum = threadInfos.size
            mIsSupportMulti = remarkPointSqlEntry(mDownloadId).supportMultiThread()
            for (threadinfo in threadInfos) {
                val downloadRunnable = DownloadRunnable(
                        url,
                        threadinfo.segment,
                        threadinfo.extSize,
                        threadinfo.sofar,
                        null,
                        threadinfo.threadId,
                        threadinfo.total,
                        threadInfos.size,
                        mIsSupportMulti,
                        this,
                        mPath,
                        threadinfo.downloadFileId)

                mList[threadinfo.threadId] = downloadRunnable
            }

            for (downloadRunnable in mList) {
                AutoAdjustThreadPool.execute(downloadRunnable.value)
            }

        }
    }

    override fun exeProgressCalc(): Long {
        var sofarTotal: Long = 0
        for (exeRunnable in mList) {
            sofarTotal += exeRunnable.value.sofar()
        }
        var time = System.currentTimeMillis() - mTime
        if (time >= SPEED_TIME_INTVAL) {
            time -= PROCESS_TIME_INTVAL
            mSpeedIncrement = (sofarTotal - mCurSofar) * ONE_SECEND_TIME / time
            mTime = System.currentTimeMillis()
            mCurSofar = sofarTotal
        }
        return sofarTotal
    }

    override fun allComplete(): Boolean {
        var allComplete = true
        for (exeRunnable in mList) {
            if (!exeRunnable.value.complete()) {
                allComplete = false
            }
        }

        if (allComplete) {
            mDownloadTaskStatus.complete(mDownloadId)
        }

        return allComplete
    }

    override fun speedIncrement(): Long {
        return mSpeedIncrement
    }

    override fun sofar(curThreadId: Int): Long {
        return mList[curThreadId]!!.sofar()
    }

    override fun pauseComplete(curThreadId: Int) {
        (mList[curThreadId])?.pauseComplete()
    }

    override fun allPauseComplete(): Boolean {
        var allPauseComplete = true
        for (exeRunnable in mList) {
            if (!exeRunnable.value.isPauseComplete()) {
                allPauseComplete = false
            }
        }
        if (allPauseComplete) {
            mDownloadTaskStatus.pauseComplete(mDownloadId)
        }
        return allPauseComplete
    }

    override fun proProgressData(progressData: ProgressData) {
        if (progressData.status == OnStatus.COMPLETE || progressData.status == OnStatus.PAUSE) {
            GlobalSingleThreadHandleProgressData.execute(ProProgressDataRunnable(progressData))
        } else {
            mRunnable.progressData = progressData
            GlobalSingleThreadHandleProgressData.remove(mRunnable)
            GlobalSingleThreadHandleProgressData.execute(mRunnable)
        }
    }

    private val mRunnable = ProProgressDataRunnable(null)

    private class ProProgressDataRunnable(var progressData: ProgressData?) : Runnable {
        override fun run() {
            progressData?.let { progressData(progressData!!) }
        }

        internal fun progressData(progressData: ProgressData) {
            if (progressData.exeProgressCalc == null) {
                Log.e("vanda", "exeProgressCalc is null")
            }

            val sofar = progressData.exeProgressCalc!!.exeProgressCalc()
            val speed = progressData.exeProgressCalc?.speedIncrement()
            val percent = sofar.toFloat() / progressData.total.toFloat()

            progressData.sofar = sofar
            progressData.speed = speed?.let { speed } ?: 0
            progressData.percent = percent

            when (progressData.status) {
                vanda.wzl.vandadownloader.core.status.OnStatus.PENGING -> {
                    progressData.recycle()
                }

                vanda.wzl.vandadownloader.core.status.OnStatus.START -> {
                    progressData.recycle()
                }

                vanda.wzl.vandadownloader.core.status.OnStatus.CONTECT -> {
                    progressData.recycle()
                }

                vanda.wzl.vandadownloader.core.status.OnStatus.PROGRESS -> {
                    Log.d("vanda", "id = ${progressData.id} sofarChild = ${progressData.sofarChild} segment = ${progressData.totalChild} totalProgress = ${progressData.sofar}  percent = ${progressData.percent} percentChild = ${progressData.percentChild} speed = ${progressData.speed} speedChild = ${progressData.speedChild}  threadId = ${progressData.threadId}")
                    progressData.exeProgressCalc?.update(progressData.fillingRemarkMultiThreadPointSqlEntry())
                    progressData.exeProgressCalc?.update(progressData.fillingRemarkPointSqlEntry())
                    MainHandler.syncProgressDataToMain(progressData)
                }

                vanda.wzl.vandadownloader.core.status.OnStatus.COMPLETE -> {
                    val allComplete = progressData.exeProgressCalc?.allComplete()
                    Log.d("vanda", "sofarChild = ${progressData.sofarChild} segment = ${progressData.totalChild} percent = ${progressData.percent} percentChild = ${progressData.percentChild} threadId = ${progressData.threadId} complete, allComplete = $allComplete")

                    progressData.exeProgressCalc?.update(progressData.fillingRemarkMultiThreadPointSqlEntry())
                    progressData.exeProgressCalc?.update(progressData.fillingRemarkPointSqlEntry())

                    if (allComplete!!) {
                        progressData.allComplete = true
                        progressData.exeProgressCalc?.deleteThreadInfo(progressData.id)
                    } else {
                        progressData.status = OnStatus.PROGRESS
                    }
                    MainHandler.syncProgressDataToMain(progressData)
                }

                vanda.wzl.vandadownloader.core.status.OnStatus.PAUSE -> {

                    Log.d("vanda", "sofarChild = ${progressData.sofarChild} segment = ${progressData.totalChild} percent = ${progressData.percent} percentChild = ${progressData.percentChild} threadId = ${progressData.threadId} pause")

                    progressData.exeProgressCalc?.update(progressData.fillingRemarkMultiThreadPointSqlEntry())
                    progressData.exeProgressCalc?.update(progressData.fillingRemarkPointSqlEntry())

                    progressData.exeProgressCalc?.pauseComplete(progressData.threadId)
                    val allPauseComplete = progressData.exeProgressCalc?.allPauseComplete()
                    Log.d("vanda", "sofarChild = ${progressData.sofarChild} segment = ${progressData.totalChild} percent = ${progressData.percent} percentChild = ${progressData.percentChild} threadId = ${progressData.threadId} pause, allPauseComplete = $allPauseComplete")
                    if (allPauseComplete!!) {
                        MainHandler.syncProgressDataToMain(progressData)
                    } else {
                        progressData.recycle()
                    }
                }

                vanda.wzl.vandadownloader.core.status.OnStatus.ERROR -> {
                    progressData.recycle()
                }

                vanda.wzl.vandadownloader.core.status.OnStatus.RETRY -> {
                    progressData.recycle()
                }
            }
        }

    }
}