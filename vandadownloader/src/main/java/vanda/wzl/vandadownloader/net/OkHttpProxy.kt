/*
 * Copyright (C) 2005-2017 UCWeb Inc. All rights reserved.
 *  Description :OkHttpProxy.java
 *
 *  Creation    : 2017-04-08
 *  Author      : zhonglian.wzl@alibaba-inc.com
 */

package vanda.wzl.vandadownloader.net

import java.util.concurrent.TimeUnit

import okhttp3.Call
import okhttp3.Dispatcher
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
            OkHttpProxy.mOkHttpClient = okHttpClient
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
