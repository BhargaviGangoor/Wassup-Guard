package com.example.wassupguard.ui.screens

import android.app.Application
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wassupguard.data.AppDatabase
import com.example.wassupguard.data.entity.ScanLog
import com.example.wassupguard.util.QuarantineManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class QuarantineViewModel(application: Application) : AndroidViewModel(application) {

    private val scanLogDao = AppDatabase.getDatabase(application).scanLogDao()

    val quarantinedFiles = scanLogDao.observeAll()
        .map { logs -> logs.filter { it.verdict == "Malicious" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteFile(log: ScanLog) {
        viewModelScope.launch {
            // Delete the log from the database
            scanLogDao.delete(log)

            // Delete the actual file from quarantine
            withContext(Dispatchers.IO) {
                val quarantinedFile = File(log.filePath)
                if (quarantinedFile.exists()) {
                    quarantinedFile.delete()
                }
            }
        }
    }

    fun restoreAndOpenFile(log: ScanLog) {
        viewModelScope.launch {
            val quarantinedFile = File(log.filePath)
            val restoredFile = QuarantineManager.restoreFile(quarantinedFile)
            if (restoredFile != null) {
                val uri = FileProvider.getUriForFile(getApplication(), "com.example.wassupguard.provider", restoredFile)
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, getMimeType(restoredFile))
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                getApplication<Application>().startActivity(intent)
            }
        }
    }

    private fun getMimeType(file: File): String? {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "pdf" -> "application/pdf"
            else -> null
        }
    }
}