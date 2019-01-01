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

package vanda.wzl.vandadownloader.multitask

import vanda.wzl.vandadownloader.core.DownloadListener

abstract class DownloadTask {
    /**
     * 下载ID
     * @return
     */
    abstract fun getId(): Int

    /**
     * 下载文件名字
     * @return
     */
    abstract fun getTitle(): String

    /**
     * 下载文件大小，对于trunk类型的下载 ，此值为0
     * @return
     */
    abstract fun getTotal(): Long

    /**
     * 已下载的大小
     * @return
     */
    abstract fun getSoFar(): Long

    /**
     * 下载状态
     * @return
     */
    abstract fun getStatus(): Int

    /**
     * 下载地址
     * @return
     */
    abstract fun getUrl(): String

    /**
     * 下载文件位置
     * @return
     */
    abstract fun getPath(): String

    /**
     * 下载的mimetype,有可能为null
     * @return
     */
    abstract fun getMimeType(): String

    /**
     *
     * 下载的ref-mUrl, 有可能为null
     * @return
     */
    abstract fun getRefUrl(): String


    /**
     * 开始任务
     */
    abstract fun start()

//    /**
//     * 是否是静默下载任务
//     * @return
//     */
//    abstract fun isSilent(): Boolean
//
//    /**
//     * 设置该任务是否是静默任务
//     * @param isSilent
//     */
//    abstract fun setIsSilent(isSilent: Boolean)

    /**
     * 暂停任务
     */
    abstract fun pause()

//    /**
//     * 删除任务
//     */
//    abstract fun delete()
//
//    /**
//     * 是否是视频缓存任务
//     * @return
//     */
//    abstract fun isVideoCache(): Boolean
//
//    /**
//     * 是否是App的升级
//     * @return
//     */
//    abstract fun isVerificationFile(): Boolean
//
//    /**
//     * App升级的信息核对,目前是:size|md5
//     * @return
//     */
//    abstract fun getVerificationFileinfo(): String

    /**
     * post任务下载的body下载格式
     * @return
     */
    abstract fun getPostBody(): String

    /**
     * 当前任务是否在Wi-Fi下下载，任务不会网络切换后重新下载，如果从wifi切换的移动网络，下载模块将会暂停该任务，重新切换到Wi-Fi网络不会自动下载
     * @return
     */
    abstract fun isWifiRequired(): Boolean

    /**
     * 设置是否要求环境执行下载任务
     * @param isWifiRequired 是否要求Wi-Fi
     */
    abstract fun setIsWifiRequired(isWifiRequired: Boolean)

    /**
     * 是否强制重新下载
     * @return
     */
    abstract fun isReforceDownload(): Boolean

    /**
     * 是否强制重新下载
     * @return
     */
    abstract fun setReforceDownload(reforceDownload: Boolean)

    /**
     * 下载请求的头部信息
     * @see QuarkDownloader.start
     * @return
     */
    abstract fun getHeaders(): Map<String, String>


    abstract fun getThreadNum(): Int




    private var mTag: Any? = null

    /**
     * 设置tag
     * @param tag
     * @return
     */
    fun setTag(tag: Any?): DownloadTask {
        if (tag != null) {
            mTag = tag
        }
        return this
    }

    /**
     * 获取设置的tag
     * @return
     */
    fun getTag(): Any? {
        return mTag
    }


    private var mDownloadListener: DownloadListener? = null

    /**
     * 添加当前任务的监听回调 @see [QuarkDownloadListener.onDownloadStateChanged]
     * @param l 监听接口
     * @return
     */
    fun addOnStateChangeListener(l: DownloadListener): DownloadTask {
        mDownloadListener = l
        return this
    }

    /**
     * 获取当前任务监听器
     * @return
     */
    fun getOnStateChangeListener(): DownloadListener? {
        return mDownloadListener
    }
}