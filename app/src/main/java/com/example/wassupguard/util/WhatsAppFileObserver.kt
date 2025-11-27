package com.example.wassupguard.util

import android.os.Build
import android.os.FileObserver
import android.util.Log
import java.io.File

class WhatsAppFileObserver(
    private val directoryPath: String,
    private val onCreate: (String) -> Unit
) {

    private var observer: FileObserver? = null

    fun startWatching() {
        if (observer != null) return
        val directory = File(directoryPath)
        if (!directory.exists()) {
            directory.mkdirs()
        }
        observer = createObserver(directory).also { it.startWatching() }
        Log.d(TAG, "Watching directory: $directoryPath")
    }

    fun stopWatching() {
        observer?.stopWatching()
        observer = null
    }

    private fun createObserver(directory: File): FileObserver {
        val mask = FileObserver.CREATE
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            object : FileObserver(directory, mask) {
                override fun onEvent(event: Int, path: String?) {
                    handleEvent(directory, event, path)
                }
            }
        } else {
            object : FileObserver(directory.absolutePath, mask) {
                override fun onEvent(event: Int, path: String?) {
                    handleEvent(directory, event, path)
                }
            }
        }
    }

    private fun handleEvent(baseDir: File, event: Int, relativePath: String?) {
        if (event and FileObserver.CREATE == 0 || relativePath.isNullOrBlank()) return
        val createdFile = File(baseDir, relativePath)
        val absolutePath = createdFile.absolutePath
        Log.v(TAG, "CREATE event: $absolutePath")
        onCreate(absolutePath)
    }

    companion object {
        private const val TAG = "WAFileObserver"

        private val REQUIRED_WHATSAPP_PATHS = listOf(
            "/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Documents",
            "/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Images",
            "/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/WhatsApp Video"
        )

        fun getMandatoryPaths(): List<String> = REQUIRED_WHATSAPP_PATHS

        fun getWhatsAppMediaPaths(): List<String> = REQUIRED_WHATSAPP_PATHS.filter { File(it).exists() }
    }
}