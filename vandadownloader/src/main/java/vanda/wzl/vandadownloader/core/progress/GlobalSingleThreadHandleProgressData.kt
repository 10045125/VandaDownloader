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

package vanda.wzl.vandadownloader.core.progress

import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object GlobalSingleThreadHandleProgressData {
    private val executors = ThreadPoolExecutor(3, 3,
            60L, TimeUnit.MILLISECONDS,
            LinkedBlockingQueue())

    fun execute(task: Runnable) {
        executors.submit(task)
    }

    fun remove(task: Runnable) {
        executors.remove(task)
    }
}