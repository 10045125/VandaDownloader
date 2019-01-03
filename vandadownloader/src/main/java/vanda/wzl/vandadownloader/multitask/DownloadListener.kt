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

package vanda.wzl.vandadownloader.multitask

interface DownloadListener {
    fun onProgress(sofar: Long, sofarChild: Long, total: Long, totalChild: Long, percent: Float, percentChild: Float, speed: Long, speedChild: Long, threadId: Int)

    fun onComplete(downloadId: Int, total: Long)

    fun onPause(downloadId: Int, sofar: Long, total: Long)
}