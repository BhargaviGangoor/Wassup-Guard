package com.example.wassupguard.util

import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

object HashUtils {
    fun sha256(file: File): String = digest(file, "SHA-256")
    fun md5(file: File): String = digest(file, "MD5")

    private fun digest(file: File, algorithm: String): String {
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        val md = MessageDigest.getInstance(algorithm)
        FileInputStream(file).use { fis ->
            var read: Int
            while (fis.read(buffer).also { read = it } != -1) {
                md.update(buffer, 0, read)
            }
        }
        return md.digest().joinToString("") { "%02x".format(it) }
    }
}


