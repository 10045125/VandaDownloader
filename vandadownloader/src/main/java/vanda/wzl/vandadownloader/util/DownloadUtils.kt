package vanda.wzl.vandadownloader.util

import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import kotlin.experimental.and

object DownloadUtils {
    /**
     * @param url  The downloading URL.
     * @param path The absolute file path.
     * @return The download id.
     */
    fun generateId(url: String, path: String): Int {
        return generateId(url, path, false)
    }

    /**
     * @param url  The downloading URL.
     * @param path If `pathAsDirectory` is `true`, `path` would be the absolute
     * directory to place the file;
     * If `pathAsDirectory` is `false`, `path` would be the absolute
     * file path.
     * @return The download id.
     */
    private fun generateId(url: String, path: String, pathAsDirectory: Boolean): Int {
        return if (pathAsDirectory) {
            md5(formatString("%sp%s@dir", url, path)).hashCode()
        } else {
            md5(formatString("%sp%s", url, path)).hashCode()
        }
    }

    private fun md5(string: String): String {
        val hash: ByteArray
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.toByteArray(charset("UTF-8")))
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException("Huh, MD5 should be supported?", e)
        } catch (e: UnsupportedEncodingException) {
            throw RuntimeException("Huh, UTF-8 should be supported?", e)
        }

        val hex = StringBuilder(hash.size * 2)
        for (b in hash) {
            if (b and 0xFF.toByte() < 0x10) hex.append("0")
            hex.append(Integer.toHexString((b and 0xFF.toByte()).toInt()))
        }
        return hex.toString()
    }


    private fun formatString(msg: String, vararg args: Any): String {
        return try {
            String.format(msg, *args)
        } catch (e: Exception) {
            ""
        }

    }
}