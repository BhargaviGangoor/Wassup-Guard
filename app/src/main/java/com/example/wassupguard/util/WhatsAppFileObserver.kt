package com.example.wassupguard.util

import android.os.FileObserver
import android.util.Log
import java.io.File

/**
 * WhatsAppFileObserver - Monitors WhatsApp media folders for new files
 * 
 * What it does:
 * - Watches WhatsApp folders for new file downloads
 * - Triggers scanning when new files are detected
 * - Only monitors PDF and image files (as per requirements)
 * 
 * Think of it like: A security guard watching a folder, alerting when new files arrive
 */
class WhatsAppFileObserver(
    private val path: String,
    private val onFileCreated: (File) -> Unit
) : FileObserver(path, CREATE or CLOSE_WRITE) {

    private val TAG = "WhatsAppFileObserver"
    
    // File extensions to monitor (PDF and images only)
    private val allowedExtensions = setOf(
        ".pdf",
        ".jpg", ".jpeg",
        ".png",
        ".gif",
        ".bmp",
        ".webp"
    )

    override fun onEvent(event: Int, path: String?) {
        if (path == null) return

        // Only process file creation or file write completion
        if (event and CREATE != 0 || event and CLOSE_WRITE != 0) {
            val file = File(this.path, path)
            
            // Wait a bit for file to be fully written
            Thread.sleep(500)
            
            if (file.exists() && file.isFile) {
                val extension = getFileExtension(file.name.lowercase())
                
                if (allowedExtensions.contains(extension)) {
                    Log.d(TAG, "New file detected: ${file.name}")
                    onFileCreated(file)
                }
            }
        }
    }

    private fun getFileExtension(filename: String): String {
        val lastDot = filename.lastIndexOf('.')
        return if (lastDot >= 0) {
            filename.substring(lastDot)
        } else {
            ""
        }
    }

    /**
     * Get WhatsApp media directories to monitor
     * WhatsApp stores media in: /storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/
     */
    companion object {
        fun getWhatsAppMediaPaths(): List<String> {
            val paths = mutableListOf<String>()
            
            // Main WhatsApp media directory
            val mainPath = "/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media"
            if (File(mainPath).exists()) {
                paths.add(mainPath)
            }
            
            // Subdirectories to monitor
            val subdirs = listOf("WhatsApp Images", "WhatsApp Documents", "WhatsApp Video")
            subdirs.forEach { subdir ->
                val fullPath = "$mainPath/$subdir"
                if (File(fullPath).exists()) {
                    paths.add(fullPath)
                }
            }
            
            // Alternative path (for some devices)
            val altPath = "/storage/emulated/0/WhatsApp/Media"
            if (File(altPath).exists()) {
                paths.add(altPath)
            }
            
            return paths
        }
    }
}

