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

import android.annotation.SuppressLint
import android.content.Context
import java.lang.RuntimeException

class DownloadContext  private constructor() {

    private var mContext: Context? = null
    private var mDefaultPath: String = ""

    private object SingleHolder {
        @SuppressLint("StaticFieldLeak")
        internal val INSTANCE = DownloadContext()
    }

    companion object {

        fun setContext(context: Context) {
            SingleHolder.INSTANCE.mContext = context
        }

        fun getContext(): Context {
            return SingleHolder.INSTANCE.mContext ?: throw RuntimeException("context is null")
        }

        fun getDefaultSaveRootPath(): String {
            return SingleHolder.INSTANCE.mDefaultPath
        }
    }

}