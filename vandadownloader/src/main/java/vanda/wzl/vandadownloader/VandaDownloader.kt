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

package vanda.wzl.vandadownloader

import android.text.TextUtils
import vanda.wzl.vandadownloader.core.DownloadContext
import vanda.wzl.vandadownloader.core.util.MimeUtils
import java.io.File

class VandaDownloader {


    class Request private constructor(/* package */
            internal var builder: Builder) {

        class Builder {

            /**
             *
             */
            /* package */
            internal var url: String? = null
            /**
             *
             */
            internal var refUrl: String? = null
            /**
             *
             */
            internal var mimeType: String? = null
            /**
             *
             */
            internal var title: String? = null
            /**
             *
             */
            internal var path: String? = null
            /**
             *
             */
            internal var isSilent: Boolean = false
            /**
             *
             */
            internal var isRename = true
            /**
             *
             */
            internal var isVideoCache: Boolean = false
            /**
             *
             */
            internal var model: Int = 0
            /**
             *
             */
            internal var isVerificationFile: Boolean = false
            /**
             *
             */
            internal var verificationFileInfo = ""
            /**
             *
             */
            internal var postBody = ""

            internal var isReforceDownload = false

            internal var isWifiRequired = false

            internal var map: Map<String, String>? = null

            internal var threadNum = 0

//            internal var fileSize = BaseDownloadTask.DEFAULE_FILESIZE

            /**
             * 下载url
             *
             * @param url
             * @return
             */
            fun url(url: String): Builder {
                this.url = url
                return this
            }

            /**
             * 下载当前的引用链接，这个链接部分服务器会校验
             *
             * @param refUrl
             * @return
             */
            fun refUrl(refUrl: String): Builder {
                this.refUrl = refUrl
                return this
            }

            /**
             * 下的文件mimeType
             *
             * @param mimeType
             * @return
             */
            fun mimeType(mimeType: String): Builder {
                this.mimeType = mimeType
                return this
            }

            /**
             * 是否是静默任务
             *
             * @param isSilent
             * @return
             */
            fun isSilent(isSilent: Boolean): Builder {
                this.isSilent = isSilent
                return this
            }

            /**
             * 是否启用内部的重命名机制
             *
             * @param isRename
             * @return
             */
            fun isRename(isRename: Boolean): Builder {
                this.isRename = isRename
                return this
            }

            /**
             * 是否是视频缓存类型，这个是为视频下载预留的，下载文件的其他业务不需要关心该字段
             *
             * @param isVideoCache
             * @return
             */
            fun isVideoCache(isVideoCache: Boolean): Builder {
                this.isVideoCache = isVideoCache
                return this
            }

            /**
             * @param model
             * @return
             */
            @Deprecated("")
            fun model(model: Int): Builder {
                this.model = model
                return this
            }

            /**
             * 是否需要下载文件校验
             *
             * @param isVerificationFile
             * @return
             */
            fun isVerificationFile(isVerificationFile: Boolean): Builder {
                this.isVerificationFile = isVerificationFile
                return this
            }

            /**
             * 文件校验字串，业务方自己要定义格式，然后文件下载完成后处理自己的文件校验逻辑
             *
             * @param verificationFileInfo
             * @return
             */
            fun verificationFileInfo(verificationFileInfo: String): Builder {
                this.verificationFileInfo = verificationFileInfo
                return this
            }

            /**
             * post请求方式的body数据
             *
             * @param postBody
             * @return
             */
            fun postBody(postBody: String): Builder {
                this.postBody = postBody
                return this
            }

            /**
             * 该任务是否启用强制重新下载，这样会覆盖相同的文件，相同文件是指文件名和路径相同
             *
             * @param isReforceDownload
             * @return
             */
            fun isReforceDownload(isReforceDownload: Boolean): Builder {
                this.isReforceDownload = isReforceDownload
                return this
            }

            /**
             * 当前任务是否在Wi-Fi下下载，任务不会网络切换后重新下载，如果从wifi切换的移动网络，下载模块将会暂停该任务，重新切换到Wi-Fi网络不会自动下载
             *
             * @param isWifiRequired
             * @return
             */
            fun isWifiRequired(isWifiRequired: Boolean): Builder {
                this.isWifiRequired = isWifiRequired
                return this
            }

            /**
             * 下载标题，即下载文件名，如果为null或空，则将url的最后一段作为文件名
             *
             * @param title
             * @return
             * @see {@link .check
             */
            fun title(title: String): Builder {
                this.title = title
                return this
            }

            /**
             * 下载的路径
             *
             * @param path
             * @return
             */
            fun path(path: String): Builder {
                this.path = path
                return this
            }

//            /**
//             * 设置文件大小
//             * @param fileSize
//             * @return
//             */
//            fun fileSize(fileSize: Long): Builder {
//                this.fileSize = fileSize
//                return this
//            }

            /**
             * 请求的头部信息
             *
             * @param map
             * @return
             */
            fun addHeaders(map: Map<String, String>): Builder {
                this.map = map
                return this
            }

            fun threadNum(threadNum: Int): Builder {
                this.threadNum = threadNum
                return this
            }

            /**
             * @return
             */
            fun build(): Request? {
                return if (!check()) {
                    null
                } else Request(this)
            }

            private fun check(): Boolean {
                if (TextUtils.isEmpty(url)) {
                    return false
                }

                if (TextUtils.isEmpty(title)) {
                    title = extractTitle(url!!, mimeType)
                }

                if (TextUtils.isEmpty(path)) {
                    path = createPath(url, title)
                }
                return true
            }

            private fun createPath(url: String?, title: String?): String {
                return DownloadContext.getDefaultSaveRootPath() + File.separator + title
            }

            companion object {

                // extract title from mUrl
                // for example: http://www.dll.com/x/y/this_file_name.apk -> this_file_name.apk
                /* package */ // for unit test
                internal fun extractTitle(url: String, mimeType: String?): String {
                    return MimeUtils.guessFileName(url, null, mimeType)
                }
            }
        }
    }
}