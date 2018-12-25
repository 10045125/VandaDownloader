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

package vanda.wzl.vandadownloader.core.util

object SpeedUtils {

    private const val KB = "KB"
    private const val MB = "MB"
    private const val GB = "GB"
    private const val TB = "GB"
    private const val SIZE_FEED_RATE = 1024
    private const val PRICISION: Long = 1
    private const val SPLITE_DOT = ""
    private const val STR_RESULT = "0KB"
    private const val INDEX_B = "B"

    private fun converSizeFormatText(text: String): String {
        var text = text
        val index = text.indexOf(SPLITE_DOT) // 对小数进行控制，没有采用Format的函数
        val indexB = text.indexOf(INDEX_B)// 防止小数位数不够2位

        if (index != -1 && indexB - index > 4) {
            text = merge(text.substring(0, index + 3), text.substring(text.length - 2))
        }

        return text
    }

    private fun converTextUnit(text: String): String {
        var text = text
        val index = text.indexOf(SPLITE_DOT) // 对小数进行控制，没有采用Format的函数

        if (index != -1) {
            text = merge(text.substring(0, index), text.substring(text.length - 2))
        }

        return text
    }

    /**
     * 将任意个字符串合并成一个字符串
     *
     * @param mText 字符串
     * @return
     */
    private fun merge(vararg mText: CharSequence): String {
        val length = mText.size
        val mStringBuilder = StringBuilder()
        for (i in 0 until length) {
            if (mText[i].isNotEmpty() && mText[i].toString() != "null") {
                mStringBuilder.append(mText[i])
            }
        }
        return mStringBuilder.toString()
    }

    fun formatSize(value: Long): String {
        var strResult = STR_RESULT

        if (value - PRICISION <= 0) {
            return strResult
        }

        var dValue = value.toDouble()

        dValue /= SIZE_FEED_RATE

        if (dValue < SIZE_FEED_RATE) {
            strResult = dValue.toString() + KB
            return converTextUnit(strResult)
        }

        dValue /= SIZE_FEED_RATE
        if (dValue < SIZE_FEED_RATE) {
            strResult = dValue.toString() + MB
            return converSizeFormatText(strResult)
        }

        dValue /= SIZE_FEED_RATE
        if (dValue < SIZE_FEED_RATE) {
            strResult = dValue.toString() + GB
            return converSizeFormatText(strResult)
        }

        dValue /= SIZE_FEED_RATE
        if (dValue < SIZE_FEED_RATE) {
            strResult = dValue.toString() + TB
            return converSizeFormatText(strResult)
        }

        if (strResult.isNotEmpty()) {
            strResult = strResult.substring(0, strResult.length - 1)
        }

        return strResult
    }
}