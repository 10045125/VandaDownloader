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

package vanda.wzl.vandadownloader.core.database

object RemarkMultiThreadPointSqlKey {
    const val TABLE_NAME_MULTI_THREAD = "remarkmultithreadpointsql"
    const val ID = "id"
    const val URL = "url"
    const val DOWNLOAD_LENGTH = "download_length"
    const val DOWNLOAD_SOFAR = "sofar"
    const val THREAD_ID = "thread_id"
    const val DOWNLOADFILE_ID = "downloadfile_id"
    const val STATUS = "status"
    const val NORMAL_SIZE = "normal_size"
    const val EXT_SIZE = "ext_size"
}