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

import java.util.concurrent.TimeUnit

import okhttp3.OkHttpClient


object OkHttpProxy {

    val CONNECT_TIMEOUT = 15 * 1000
    val SOCKET_TIMEOUT = 30 * 1000
    private var mOkHttpClient: OkHttpClient? = null

    /**
     * 不建议调用该方法
     * @param okHttpClient
     */
    var instance: OkHttpClient
        get() = if (mOkHttpClient == null) init() else mOkHttpClient!!
        set(okHttpClient) {
            mOkHttpClient = okHttpClient
        }

    private fun init(): OkHttpClient {
        synchronized(OkHttpProxy::class.java) {
            if (mOkHttpClient == null) {
                val build = OkHttpClient.Builder()
                        .connectTimeout(CONNECT_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)
                        .readTimeout(SOCKET_TIMEOUT.toLong(), TimeUnit.MILLISECONDS)

                //                try {
                //                    // Install the all-trusting trust manager
                //                    final SSLContext sslcontext = SSLContext.getInstance("SSL");
                //                    X509TrustManager x509TrustManager = configX509TrustManager();
                //                    sslcontext.init(null, new TrustManager[]{x509TrustManager}, new java.security.SecureRandom());
                //                    build.sslSocketFactory(sslcontext.getSocketFactory(), x509TrustManager);
                //                    build.hostnameVerifier(configX509HostnameVerifier());
                //
                //                } catch (Exception e) {
                //                    e.printStackTrace();
                //                }
                mOkHttpClient = build.build()
            }
        }
        return mOkHttpClient!!
    }

    fun cancel(tag: Any) {
        val dispatcher = instance.dispatcher()
        for (call in dispatcher.queuedCalls()) {
            if (tag == call.request().tag()) {
                call.cancel()
            }
        }
        for (call in dispatcher.runningCalls()) {
            if (tag == call.request().tag()) {
                call.cancel()
            }
        }
    }
}
