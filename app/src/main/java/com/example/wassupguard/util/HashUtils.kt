package com.example.wassupguard.util

import java.io.File
import java.io.FileInputStream
import java.security.DigestInputStream
import java.security.MessageDigest

object HashUtils {

    fun sha256(file: File): String = digest(file, "SHA-256")

    fun sha256(path: String): String = sha256(File(path))

    private fun digest(file: File, algorithm: String): String {
        val messageDigest = MessageDigest.getInstance(algorithm)
        FileInputStream(file).use { fis ->
            DigestInputStream(fis, messageDigest).use { dis ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                while (dis.read(buffer) != -1) {
                    // DigestInputStream updates the hash automatically
                }
            }
        }
        return messageDigest.digest().joinToString("") { "%02x".format(it) }
    }
}
