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

package vanda.wzl.vandadownloader.core.util

import android.text.TextUtils
import android.util.Base64
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*

class MIMEParameterParser {
    private class ParameterValue {
        private var mCharset: String? = null
        private var mLanguage: String? = null
        private var mValue: String? = null

        fun charset(): String? {
            return mCharset
        }

        fun language(): String? {
            return mLanguage
        }

        fun value(): String? {
            return mValue
        }

        fun setCharset(charset: String?) {
            mCharset = charset
        }

        fun setLanguage(language: String?) {
            mLanguage = language
        }

        fun setValue(value: String?) {
            mValue = value
        }
    }

    private class Parameter : Comparable<Parameter> {
        private var mSection: Int = 0
        var isExtended = false
        var isMultiPartValue = false
            private set
        private var mValues: MutableList<ParameterValue>? = null

        val values: List<ParameterValue>?
            get() = mValues

        override fun compareTo(o: Parameter): Int {
            return if (o == null) {
                1
            } else {
                mSection - o.mSection
            }
        }

        fun section(): Int {
            return mSection
        }

        fun setSection(section: Int) {
            mSection = section
        }

        fun addValue(subValue: ParameterValue?) {
            if (subValue == null) {
                return
            }
            if (mValues == null) {
                mValues = ArrayList()
                isMultiPartValue = false
            } else {
                isMultiPartValue = true
            }
            mValues!!.add(subValue)
        }
    }

    /**
     * 解析 RFC2184 定义的参数
     *
     * @param name:        参数名，不能为空
     * @param aParameters: 待解析的参数字符串，不能为空
     * @return 参数值
     * @see http://tools.ietf.org/html/rfc2184
     */
    fun parse(name: String, aParameters: String): String? {
        val isNameEmpty = TextUtils.isEmpty(name)
        val isParametersEmpty = TextUtils.isEmpty(aParameters)
        if (isNameEmpty || isParametersEmpty) {
            return null
        }
        // 1 split parameters
        val stringList = split(aParameters, ';')
        if (stringList == null || stringList.size == 0) {
            return null
        }
        // 2 parse parameters
        val parameters = ArrayList<Parameter>()
        for (string in stringList) {
            val parameter = parseParameter(name, string)
            if (parameter != null) {
                parameters.add(parameter)
            }
        }
        if (parameters.size == 0) {
            return null
        }
        // 3 sort by section
        Collections.sort(parameters)
        // 4 decode and merge value
        var hasInitCharset = false
        var charset: String? = null
        var language: String? = null
        val stringBuilder = StringBuilder()
        for (parameter in parameters) {
            if (parameter == null) {
                continue
            }
            val values = parameter.values
            if (values == null || values.size == 0) {
                continue
            }
            for (value in values) {
                if (value == null) {
                    continue
                }
                if (!hasInitCharset) {
                    hasInitCharset = true
                    charset = value.charset()
                    language = value.language()
                }
                if (TextUtils.isEmpty(value.value())) {
                    continue
                }
                if (parameter.isMultiPartValue) {
                    stringBuilder.append(decodeValue(value.value(), value.charset(), value.language()))
                } else if (parameter.isExtended) {
                    stringBuilder.append(decodeValue(value.value(), charset, language))
                } else {
                    stringBuilder.append(decodeValue(value.value(), "", ""))
                }
            }
        }
        return stringBuilder.toString()
    }

    private fun split(aString: String, splitCharacter: Char): List<String>? {
        var aString = aString
        aString = if (TextUtils.isEmpty(aString)) aString else aString.trim { it <= ' ' }
        if (TextUtils.isEmpty(aString)) {
            return null
        }
        var beginIndex = 0
        var quoteCharacter: Char = 0.toChar()
        val stringList = ArrayList<String>()
        for (i in 0 until aString.length) {
            val character = aString[i]
            if (character == '\'' || character == '"') {
                if (quoteCharacter == character) {
                    quoteCharacter = 0.toChar()
                } else {
                    quoteCharacter = character
                }
            } else if (character == splitCharacter && quoteCharacter.toInt() == 0) {
                val subString = aString.substring(beginIndex, i)
                val splitString = if (TextUtils.isEmpty(subString)) subString else subString.trim { it <= ' ' }
                beginIndex = i + 1
                if (!TextUtils.isEmpty(splitString)) {
                    stringList.add(splitString)
                }
            }
        }
        if (beginIndex < aString.length) {
            val sub = aString.substring(beginIndex)
            val splitString = if (TextUtils.isEmpty(sub)) sub else sub.trim { it <= ' ' }
            if (!TextUtils.isEmpty(splitString)) {
                stringList.add(splitString)
            }
        }
        return stringList
    }

    /**
     * 去除字符串两端的空格
     *
     * @param originalString 原始字符串
     * @return 返回去除两端的空格后的字符串
     */
    fun trim(originalString: String): String? {
        return originalString?.trim { it <= ' ' }
    }

    private fun parseParameter(aName: String, aParameter: String): Parameter? {
        if (TextUtils.isEmpty(aParameter)) {
            return null
        }
        val index = aParameter.indexOf('=')
        if (index != -1) {
            var parameter: Parameter? = null
            val name = trim(aParameter.substring(0, index))
            if (TextUtils.isEmpty(name)) {
                return null
            }
            parameter = parseName(aName, name!!)
            if (parameter == null) {
                return null
            }
            var value: String? = null
            if (index + 1 < aParameter.length) {
                value = trim(aParameter.substring(index + 1))
            }
            if (!TextUtils.isEmpty(value) && value!![0] == '"' && value[value.length - 1] == '"') {
                if (value.length <= 2) {
                    return null
                }
                value = trim(value.substring(1, value.length - 1))
            }
            if (TextUtils.isEmpty(value)) {
                return parameter
            }
            if (parseValue(parameter, value)) {
                return parameter
            }
        }
        return null
    }

    private fun parseName(aName: String, aString: String): Parameter? {
        if (TextUtils.isEmpty(aString)) {
            return null
        }
        val parameter = Parameter()
        val firstAsteriskIndex = aString.indexOf('*')

        // *号不存在
        if (firstAsteriskIndex == -1) {
            if (!aName.equals(aString, ignoreCase = true)) {
                return null
            }
            parameter.setSection(1)
            parameter.isExtended = false
            return parameter
        }

        if (!aName.equals(trim(aString.substring(0, firstAsteriskIndex))!!, ignoreCase = true)) {
            return null
        }

        // 仅*号在末尾
        if (firstAsteriskIndex == aString.length - 1) {
            parameter.setSection(1)
            parameter.isExtended = true
            return parameter
        }

        var section: String? = null
        // *号在末尾，且含section
        if (aString[aString.length - 1] == '*') {
            section = trim(aString.substring(firstAsteriskIndex + 1, aString.length - 1))
            parameter.isExtended = true
        } else { // *号不在末尾，但含section
            section = trim(aString.substring(firstAsteriskIndex + 1))
            parameter.isExtended = false
        }

        if (TextUtils.isEmpty(section)) {
            return null
        }

        parameter.setSection(parseInt(section, -1))
        return if (parameter.section() >= 0) {
            parameter
        } else null

    }

    fun parseInt(aValue: String?, aDefault: Int): Int {
        var aValue = aValue
        if (aValue == null || aValue.length == 0) {
            return aDefault
        }
        var result = aDefault
        val isHex = aValue.startsWith("0x")
        if (isHex) {
            aValue = aValue.substring(2)
        }
        try {
            if (!isHex) {
                result = Integer.parseInt(aValue)
            } else {
                result = java.lang.Long.parseLong(aValue, 16).toInt()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return result
    }

    private fun parseValue(parameter: Parameter?, aValue: String?): Boolean {
        var aValue = aValue
        if (parameter == null) {
            return false
        }
        val parameterValue = ParameterValue()
        if (parameter.section() > 1 || TextUtils.isEmpty(aValue)) {
            parameterValue.setValue(aValue)
            parameter.addValue(parameterValue)
            return true
        }
        if (aValue!!.startsWith("=?") && aValue.endsWith("?=")) {
            if (aValue.length < 4) {
                return false
            }
            parameter.isExtended = true
            return parseEncodedWordValue(parameter, aValue)
        } else if (parameter.isExtended) {
            val firstQuoteIndex = aValue.indexOf('\'')
            if (firstQuoteIndex == -1) {
                parameterValue.setValue(aValue)
                parameter.addValue(parameterValue)
                return true
            }
            if (firstQuoteIndex == aValue.length - 1) {
                return false
            }
            parameterValue.setCharset(trim(aValue.substring(0, firstQuoteIndex)))
            aValue = trim(aValue.substring(firstQuoteIndex + 1))
            if (TextUtils.isEmpty(aValue)) {
                return false
            }
            val secondQuoteIndex = aValue!!.indexOf('\'')
            if (secondQuoteIndex == -1) {
                return false
            }
            parameterValue.setLanguage(trim(aValue.substring(0, secondQuoteIndex)))
            if (secondQuoteIndex < aValue.length - 1) {
                parameterValue.setValue(trim(aValue.substring(secondQuoteIndex + 1)))
                parameter.addValue(parameterValue)
            }
            return true
        } else {
            parameterValue.setValue(aValue)
            parameter.addValue(parameterValue)
            return true
        }
    }

    private fun parseEncodedWordValue(parameter: Parameter, aValue: String): Boolean {
        val valueList = aValue.split("\\?=\\s=\\?".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        if (valueList.isEmpty()) {
            return true
        }
        valueList[0] = trim(valueList[0]).toString()
        valueList[0] = valueList[0].substring(2)
        val lastIndex = valueList.size - 1
        valueList[lastIndex] = trim(valueList[lastIndex]).toString()
        valueList[lastIndex] = valueList[lastIndex].substring(0, valueList[lastIndex].length - 2)
        for (i in valueList.indices) {
            val value = trim(valueList[i])
            if (TextUtils.isEmpty(value)) {
                continue
            }
            val subValue = parseEncodedWordSubValue(value!!) ?: return false
            parameter.addValue(subValue)
        }
        return true
    }

    private fun parseEncodedWordSubValue(aValue: String): ParameterValue? {
        val value = trim(aValue!!)
        if (TextUtils.isEmpty(value)) {
            return null
        }
        val lastQuoteIndex = value!!.lastIndexOf('?')
        if (lastQuoteIndex == -1) {
            return null
        }
        var firstAsteriskIndex = value.indexOf('*')
        if (firstAsteriskIndex == -1) {
            firstAsteriskIndex = value.indexOf('?')
        } else if (firstAsteriskIndex > lastQuoteIndex) {
            return null
        }
        val parameterValue = ParameterValue()
        if (firstAsteriskIndex == -1 || firstAsteriskIndex == lastQuoteIndex) {
            parameterValue.setCharset(value.substring(0, lastQuoteIndex))
        } else {
            parameterValue.setCharset(value.substring(0, firstAsteriskIndex))
            if (firstAsteriskIndex + 1 < lastQuoteIndex) {
                parameterValue.setLanguage(value.substring(firstAsteriskIndex + 1, lastQuoteIndex))
            }
        }
        if (lastQuoteIndex + 1 < value.length) {
            parameterValue.setValue(trim(value.substring(lastQuoteIndex + 1)))
        }
        return parameterValue
    }

    private fun doUrlDecode(value: String?): String? {
        var value = value
        // from DownloadFileController.java
        if (TextUtils.isEmpty(value)) {
            return value
        }
        try {
            val urlDecodeValue = URLDecoder.decode(value!!, "ISO_8859_1")
            val urlEncodeValue = URLEncoder.encode(urlDecodeValue, "ISO_8859_1")
            if (value.equals(urlEncodeValue, ignoreCase = true)) {
                value = urlDecodeValue
            } else {
                val tempJsEncoded = value.replace("!", "%21").replace("'", "%27").replace("(", "%28").replace(")", "%29").replace("~", "%7E")
                val tempJavaEncoded = urlEncodeValue.replace("+", "%20")
                if (tempJsEncoded.equals(tempJavaEncoded, ignoreCase = true)) {
                    value = urlDecodeValue
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return value
    }

    fun base64Decode(base64Array: ByteArray): ByteArray {
        return Base64.decode(base64Array, Base64.DEFAULT)
    }

    fun base64Decode(str: String?): ByteArray {
        return Base64.decode(str, Base64.DEFAULT)
    }

    private fun decodeValue(value: String?, charset: String?, language: String?): String {
        var ret = ""
        if (TextUtils.isEmpty(value)) {
            return ret
        }
        try {
            var data: ByteArray? = null
            if ("B".equals(language!!, ignoreCase = true)) {
                data = base64Decode(value)
            } else {
                data = doUrlDecode(value)!!.toByteArray(charset("ISO_8859_1"))
            }
            var decodedValue: String? = null
            if (data == null) {
                return ret
            }
            var isDecodeSuccess = false
            if (!TextUtils.isEmpty(charset)) {
                try {
                    decodedValue = String(data, Charset.forName(charset!!))
                    isDecodeSuccess = true
                } catch (e: Exception) {
                }

            }
            if (!isDecodeSuccess) {
                if (detectUtf8(data)) {
                    decodedValue = String(data, Charset.forName("UTF-8"))
                } else {
                    decodedValue = String(data, Charset.forName("GBK"))
                }
            }
            if (!TextUtils.isEmpty(decodedValue)) {
                ret = decodedValue!!
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ret
    }

    // copy from TextUtils.cpp's detectUTFImpl()
    fun detectUtf8(data: ByteArray?): Boolean {
        if (null == data || 0 == data.size) {
            return false
        }

        var checkLen = 0
        var seqLen = 0
        var index = 0
        var oldIndex = 0
        var checkChar = 0
        var srcChar = 0

        while (true) {
            srcChar = 0xFF and data[index].toInt()

            if (srcChar and 0x80 == 0) {
                seqLen = 1
            } else if (srcChar and 0xC0 != 0xC0) {
                seqLen = 0
            } else if (srcChar and 0xE0 == 0xC0) {
                seqLen = 2
            } else if (srcChar and 0xF0 == 0xE0) {
                seqLen = 3
            } else if (srcChar and 0xF8 == 0xF0) {
                seqLen = 4
            } else if (srcChar and 0xFC == 0xF8) {
                seqLen = 5
            } else if (srcChar and 0xFE == 0xFC) {
                seqLen = 6
            }

            if (0 == seqLen) {
                return false
            }

            checkLen = seqLen
            oldIndex = index
            checkChar = 0

            // 检查UTF格式
            index += seqLen

            if (index > data.size) {
                return false
            }

            // 6字节
            if (checkLen == 6) {
                checkChar = 0xFF and data[oldIndex + 5].toInt()
                if (checkChar < 0x80 || checkChar > 0xBF) {
                    return false
                }

                checkLen--
            }

            // 5字节
            if (checkLen == 5) {
                checkChar = 0xFF and data[oldIndex + 4].toInt()
                if (checkChar < 0x80 || checkChar > 0xBF) {
                    return false
                }

                checkLen--
            }

            // 4字节
            if (checkLen == 4) {
                checkChar = 0xFF and data[oldIndex + 3].toInt()
                if (checkChar < 0x80 || checkChar > 0xBF) {
                    return false
                }

                checkLen--
            }

            // 3字节
            if (checkLen == 3) {
                checkChar = 0xFF and data[oldIndex + 2].toInt()
                if (checkChar < 0x80 || checkChar > 0xBF) {
                    return false
                }

                checkLen--
            }

            // 2字节
            if (checkLen == 2) {
                checkChar = 0xFF and data[oldIndex + 1].toInt()
                if (checkChar > 0xBF) {
                    return false
                }

                when (srcChar) {
                    // // no fall-through in this inner switch
                    // case 0xE0: if (checkChar < 0xA0) return false;
                    // case 0xED: if (checkChar > 0x9F) return false;
                    // case 0xF0: if (checkChar < 0x90) return false;
                    // case 0xF4: if (checkChar > 0x8F) return false;
                    else -> if (checkChar < 0x80) {
                        return false
                    }
                }

                checkLen--
            }

            // 1字节
            if (checkLen == 1) {
                if (srcChar >= 0x80 && srcChar < 0xC2) {
                    return false
                }
            }

            // if (srcChar > 0xF4)
            // return false;

            if (index == data.size) {
                return true
            }
        }
    }
}