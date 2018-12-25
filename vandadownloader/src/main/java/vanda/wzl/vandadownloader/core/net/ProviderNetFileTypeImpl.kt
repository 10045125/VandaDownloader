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

package vanda.wzl.vandadownloader.core.net

import android.text.TextUtils
import android.util.Log
import okhttp3.Request
import vanda.wzl.vandadownloader.core.DownloadTaskSchedule
import java.io.InputStream
import java.net.HttpURLConnection

class ProviderNetFileTypeImpl(url: String) : ProviderNetFileType() {

    private var mIsSupportMultiThread: Boolean = false
    private var mFileSize: Long? = -1
    private var mInputStream: InputStream? = null

    init {
        mInputStream = analysisFileAttributes(url)
    }

    override fun isSupportMulti(): Boolean {
        return mIsSupportMultiThread
    }

    override fun fileSize(): Long {
        return mFileSize?.let { mFileSize } ?: -1
    }

    override fun firstIntactInputStream(): InputStream {
        return mInputStream!!
    }

    private fun analysisFileAttributes(url: String): InputStream? {
        val mRequestBuilder = Request.Builder()
        mRequestBuilder.addHeader("Range", "bytes=0-${DownloadTaskSchedule.SEEK_SIZE}")
        var request = mRequestBuilder.url(url).get().build()
        var mCall = OkHttpProxy.instance.newCall(request)

        var mResponse = mCall.execute()
        val code = mResponse.code()

        // ------- 如果同时收到了Transfer-Encoding字段和Content-Length头字段，那么必须忽略Content-Length字段

        val transferEncoding = mResponse.header("Transfer-Encoding")

        if (TextUtils.equals(transferEncoding, DownloadTaskSchedule.TYPE_CHUNKED)) {
            mIsSupportMultiThread = false
        } else {
            mIsSupportMultiThread = code == HttpURLConnection.HTTP_PARTIAL && !TextUtils.isEmpty(mResponse.header("Content-Range"))
            mFileSize = mResponse.header("Content-Length")?.toLong()
            Log.e("vanda", "transferEncoding=$transferEncoding    content-length=$mFileSize")
        }

        Log.e("vanda", "mIsSupportMultiThread=$mIsSupportMultiThread")

        if (mIsSupportMultiThread && TextUtils.isEmpty(transferEncoding) && !mResponse.header("Content-Length").isNullOrBlank()) {
            mRequestBuilder.addHeader("Range", "bytes=0-")
            request = mRequestBuilder.url(url).get().build()
            mCall = OkHttpProxy.instance.newCall(request)
            mResponse = mCall.execute()
            mFileSize = mResponse.header("Content-Length")?.toLong()
        }

        return mResponse.body()?.byteStream()
    }
}