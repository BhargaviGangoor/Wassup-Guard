package com.example.wassupguard.util

import android.content.Context
import android.os.Environment
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object QuarantineManager {

    private const val TAG = "QuarantineManager"
    private const val QUARANTINE_PATH = "/storage/emulated/0/MyAntivirus/Quarantine"

    fun quarantineFile(file: File, context: Context): File? {
        if (!file.exists() || !file.isFile) {
            Log.w(TAG, "Cannot quarantine missing file: ${file.absolutePath}")
            return null
        }

        return runCatching {
            val quarantineDir = resolveQuarantineDir(context)
            val safeDestination = buildDestination(quarantineDir, file.name)
            copyFile(file, safeDestination)
            if (file.delete()) {
                Log.i(TAG, "File quarantined at ${safeDestination.absolutePath}")
            } else {
                Log.w(TAG, "Original file deletion failed: ${file.absolutePath}")
            }
            safeDestination
        }.onFailure { Log.e(TAG, "Failed to quarantine ${file.absolutePath}", it) }
            .getOrNull()
    }

    private fun resolveQuarantineDir(context: Context): File {
        val base = if (Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED) {
            File(QUARANTINE_PATH)
        } else {
            File(context.filesDir, "quarantine")
        }
        if (!base.exists()) {
            base.mkdirs()
        }
        return base
    }

    private fun buildDestination(directory: File, originalName: String): File {
        var candidate = File(directory, originalName)
        val name = candidate.nameWithoutExtension
        val extension = candidate.extension.takeIf { it.isNotEmpty() }?.let { ".$it" } ?: ""
        var suffix = 1
        while (candidate.exists()) {
            candidate = File(directory, "$name-$suffix$extension")
            suffix++
        }
        return candidate
    }

    private fun copyFile(source: File, destination: File) {
        FileInputStream(source).use { input ->
            FileOutputStream(destination).use { output ->
                input.copyTo(output)
            }
        }
    }
}