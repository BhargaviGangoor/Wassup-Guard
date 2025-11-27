package com.example.wassupguard.monitor

import android.content.Context
import android.util.Log
import com.example.wassupguard.util.HashUtils
import com.example.wassupguard.util.QuarantineManager
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class FileScanner(
    context: Context,
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.IO,
    private val callbackDispatcher: CoroutineDispatcher = Dispatchers.Main
) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + workerDispatcher)

    fun scanFile(path: String) {
        scope.launch {
            val status = runCatching { processFile(path) }
                .onFailure { Log.e(TAG, "Failed to scan $path", it) }
                .getOrNull() ?: ScanStatus.SAFE

            withContext(callbackDispatcher) {
                ScanResultHandler.onScanCompleted(path, status)
            }
        }
    }

    private suspend fun processFile(path: String): ScanStatus = withContext(workerDispatcher) {
        val file = File(path)
        if (!file.exists() || !file.isFile) {
            Log.w(TAG, "Ignoring missing file: $path")
            return@withContext ScanStatus.SAFE
        }

        val hash = HashUtils.sha256(file)
        val result = checkHashWithServer(hash)
        if (result == ScanStatus.MALICIOUS) {
            QuarantineManager.quarantineFile(file, appContext)
        }
        result
    }

    private suspend fun checkHashWithServer(hash: String): ScanStatus = withContext(workerDispatcher) {
        delay(150) // Simulate network latency for placeholder implementation
        return@withContext when {
            hash.endsWith("0") || hash.endsWith("5") -> ScanStatus.MALICIOUS
            hash.endsWith("2") || hash.endsWith("7") -> ScanStatus.SUSPICIOUS
            else -> ScanStatus.SAFE
        }
    }

    companion object {
        private const val TAG = "FileScanner"
    }
}

enum class ScanStatus {
    SAFE,
    SUSPICIOUS,
    MALICIOUS
}

object ScanResultHandler {
    @Volatile
    var callback: ((String, ScanStatus) -> Unit)? = null

    fun onScanCompleted(filePath: String, scanStatus: ScanStatus) {
        callback?.invoke(filePath, scanStatus)
    }
}

