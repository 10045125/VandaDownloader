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

package vanda.wzl.vandadownloader.core.threadpool

import android.util.Log
import java.util.concurrent.*

object AutoAdjustThreadPool {

    /**
     * 队列阈值，超过此值则扩大线程池
     */
    private const val MAX_QUEUE_SIZE = 0

    /**
     * 每次扩容自动增加线程数
     */
    private const val PER_ADD_THREAD = 9

    /**
     * 监控积压时间频率
     */
    private const val MONITOR_DELAY_TIME = 1L

    private var scheduledExecutorService: ScheduledExecutorService? = null

    private var executor: ThreadPoolExecutor? = null

    private var mIsInit = false

    fun start() {
        executor = ThreadPoolExecutor(3, Int.MAX_VALUE,
            60L, TimeUnit.MILLISECONDS,
            LinkedBlockingQueue())
        scheduledExecutorService = ScheduledThreadPoolExecutor(3, Executors.defaultThreadFactory())
        scheduledExecutorService?.scheduleWithFixedDelay({
            //            System.out.println("当前线程池状态！$executor")
            //当队列大小超过限制，且jvm内存使用率小于80%时扩容，防止无限制扩容
            if (executor!!.queue.size >= MAX_QUEUE_SIZE && executor!!.poolSize < executor!!.maximumPoolSize && getMemoryUsage() < 0.8f) {
//                System.out.println("线程池扩容！$executor")
                executor!!.corePoolSize = executor!!.poolSize + PER_ADD_THREAD
            }
            //当队列大小小于限制的80%，线程池缩容
            if (executor!!.poolSize > 0 && executor!!.queue.size < MAX_QUEUE_SIZE * 0.8) {
//                System.out.println("线程池缩容！$executor");
                executor!!.corePoolSize = executor!!.poolSize - PER_ADD_THREAD
            }
        }, MONITOR_DELAY_TIME, MONITOR_DELAY_TIME, TimeUnit.SECONDS)

        mIsInit = true
    }

    /**
     * 获取jvm内存使用率
     * @return
     */
    private fun getMemoryUsage(): Float {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / Runtime.getRuntime().maxMemory().toFloat()
    }

    fun stop() {
        if (mIsInit) {
            executor?.shutdown()
            while (!(executor?.awaitTermination(1, TimeUnit.SECONDS))!!) {
            }
            scheduledExecutorService?.shutdown()
            executor = null
            scheduledExecutorService = null
            mIsInit = false
        }
    }

    fun execute(task: Runnable): Future<*>? {
        if (!mIsInit) {
            start()
        }
        Log.i("vanda", "executor?.poolSize = ${executor?.poolSize}" + "executor?.activeCount = ${executor?.activeCount}")
        executor?.poolSize
        return executor?.submit(task)
    }

    fun remove(task: Runnable) {
        executor?.remove(task)
    }
}