/*
 * Copyright (c) 2019 YY Inc
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

package vanda.wzl.vandadownloader

import vanda.wzl.vandadownloader.multitask.DownloadTask

internal class DownloadTaskImpl(id: Int, request: VandaDownloader.Request) : DownloadTask() {

    /**
     * 下载的任务id
     */
    private val mId: Int = id
    /**
     * 下载的url
     */
    private var mUrl: String = request.builder.url
    /**
     * 下载路径
     */
    private var mPath: String = request.builder.path
    /**
     * 文件的名
     */
    private var mTitle: String = request.builder.title
    /**
     * 文件的mimeType
     */
    private var mMimeType: String = request.builder.mimeType
    /**
     * 文件的引用链接
     */
    private var mRefUrl: String = request.builder.refUrl
    /**
     * 是否是静默任务
     */
    private var mIsSilent: Boolean = request.builder.isSilent
    /**
     * post请求的body
     */
    private var mPostBody: String = request.builder.postBody

    /**
     * 当前任务是否强制重新下载
     */
    private var mIsReforceDownload = request.builder.isReforceDownload

    /**
     * 是否要求在Wifi条件下下载
     */
    private var mIsWifiRequired = request.builder.isWifiRequired

    private var mMap: Map<String, String> = request.builder.map

    private var mThreadNum: Int = request.builder.threadNum


    override fun getId(): Int {
        return mId
    }

    override fun getTitle(): String {
        return mTitle
    }

    override fun getPath(): String {
        return mPath
    }

    override fun getUrl(): String {
        return mUrl
    }

    override fun getRefUrl(): String {
        return mRefUrl
    }

    override fun getMimeType(): String {
        return mMimeType
    }



    override fun getTotal(): Long {
            return VandaDownloader.getTotal(mId)
    }

    override fun getStatus(): Int {
       return VandaDownloader.getStatus(mId)
    }


    override fun getSoFar(): Long {
        return VandaDownloader.getSoFar(mId)
    }

    override fun start() {
        MainHandler.remarkDownloadListener(this)
        VandaDownloader.start(mUrl, mThreadNum,  mPath, 200, 200, 5, mIsReforceDownload, mMap, mIsWifiRequired, mPostBody)
    }


    override fun pause() {
        VandaDownloader.pause(mId)
    }

    override fun getPostBody(): String {
        return mPostBody
    }


    override fun isWifiRequired(): Boolean {
        return mIsWifiRequired
    }

    override fun setIsWifiRequired(isWifiRequired: Boolean) {
        mIsWifiRequired = isWifiRequired
    }

    override fun isReforceDownload(): Boolean {
        return mIsReforceDownload
    }

    override fun setReforceDownload(reforceDownload: Boolean) {
        mIsReforceDownload = reforceDownload
    }

    override fun getHeaders(): Map<String, String> {
        return mMap
    }

    override fun getThreadNum(): Int {
        return mThreadNum
    }

//    fun readFromCursor(c: Cursor): DownloadTask {
//        val remarkPointSqlEntry = RemarkPointSqlEntry(c)
//        val request = VandaDownloader.Request.Builder()
//                .url(remarkPointSqlEntry.url)
//                .title(remarkPointSqlEntry.title)
//                .path(remarkPointSqlEntry.path)
//                .mimeType(remarkPointSqlEntry.mimeType)
//                .isSilent(remarkPointSqlEntry.isSilent)
//                .isVideoCache(remarkPointSqlEntry.isVideoCache)
//                .refUrl(remarkPointSqlEntry.refUrl)
//                .postBody(remarkPointSqlEntry.postBody)
//                .build()
//        return DownloadTaskImpl(remarkPointSqlEntry.id!!, request)
//    }
}