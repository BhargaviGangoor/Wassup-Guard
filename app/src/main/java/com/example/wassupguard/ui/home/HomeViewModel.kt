package com.example.wassupguard.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.wassupguard.data.AppDatabase
import com.example.wassupguard.data.dao.ScanLogDao
import com.example.wassupguard.data.entity.ScanLog
import com.example.wassupguard.workers.FileMonitorWorker
import com.example.wassupguard.util.safety.SafetyTip
import com.example.wassupguard.util.safety.SafetyTipsProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class HomeUiState(
    val scanLogs: List<ScanLog> = emptyList(),
    val totalScans: Int = 0,
    val threatsDetected: Int = 0,
    val lastScanTimestamp: Long? = null,
    val statusMessage: String = "Ready",
    val backgroundProtectionEnabled: Boolean = false,
    val isSchedulingScan: Boolean = false,
    val safetyTips: List<SafetyTip> = emptyList(),
    val isFolderSelectionRequired: Boolean = true
)

class HomeViewModel(
    private val scanLogDao: ScanLogDao,
    private val workManager: WorkManager,
    private val tipsProvider: SafetyTipsProvider = SafetyTipsProvider(),
    private val appContext: Context
) : ViewModel() {

    private val statusMessage = MutableStateFlow("Ready")
    private val backgroundProtection = MutableStateFlow(false)
    private val scheduling = MutableStateFlow(false)
    private val tipsFlow = MutableStateFlow(tipsProvider.tipsOfTheDay())
    private val _selectedDirectoryUri = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        scanLogDao.observeAll(),
        statusMessage,
        backgroundProtection,
        scheduling,
        tipsFlow,
        _selectedDirectoryUri
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val logs = values[0] as List<ScanLog>
        val status = values[1] as String
        val backgroundEnabled = values[2] as Boolean
        val schedulingScan = values[3] as Boolean
        @Suppress("UNCHECKED_CAST")
        val tips = values[4] as List<SafetyTip>
        val directoryUri = values[5] as? String

        HomeUiState(
            scanLogs = logs.take(MAX_RECENT_LOGS),
            totalScans = logs.size,
            threatsDetected = logs.count { it.verdict.contains("malicious", ignoreCase = true) },
            lastScanTimestamp = logs.firstOrNull()?.timestampEpochMillis,
            statusMessage = status,
            backgroundProtectionEnabled = backgroundEnabled,
            isSchedulingScan = schedulingScan,
            safetyTips = tips,
            isFolderSelectionRequired = directoryUri == null
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    init {
        loadSelectedDirectory()
    }

    private fun loadSelectedDirectory() {
        val sharedPrefs = appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        _selectedDirectoryUri.value = sharedPrefs.getString("whatsapp_uri", null)
    }

    fun onDirectorySelected(uri: String) {
        val sharedPrefs = appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putString("whatsapp_uri", uri).apply()
        _selectedDirectoryUri.value = uri
    }

    fun runQuickScan() {
        viewModelScope.launch {
            if (_selectedDirectoryUri.value == null) {
                statusMessage.value = "Please select a folder first"
                return@launch
            }
            scheduling.value = true
            val request = OneTimeWorkRequestBuilder<FileMonitorWorker>()
                .setInputData(workDataOf("directory_uri" to _selectedDirectoryUri.value))
                .addTag(MANUAL_SCAN_TAG)
                .build()
            workManager.enqueueUniqueWork(
                MANUAL_SCAN_TAG,
                ExistingWorkPolicy.REPLACE,
                request
            )
            statusMessage.value = "Quick scan scheduled..."
            scheduling.value = false
        }
    }

    fun toggleBackgroundProtection(enable: Boolean) {
        viewModelScope.launch {
            if (enable && _selectedDirectoryUri.value == null) {
                statusMessage.value = "Please select a folder first"
                backgroundProtection.value = false // reset toggle
                return@launch
            }

            backgroundProtection.value = enable
            if (enable) {
                val periodicRequest = PeriodicWorkRequestBuilder<FileMonitorWorker>(
                    15, TimeUnit.MINUTES
                ).setInputData(workDataOf("directory_uri" to _selectedDirectoryUri.value))
                .addTag(BACKGROUND_WORK_TAG).build()
                workManager.enqueueUniquePeriodicWork(
                    BACKGROUND_WORK_TAG,
                    ExistingPeriodicWorkPolicy.KEEP,
                    periodicRequest
                )
                statusMessage.value = "Background guard armed"
            } else {
                workManager.cancelUniqueWork(BACKGROUND_WORK_TAG)
                statusMessage.value = "Background guard paused"
            }
        }
    }

    companion object {
        private const val MANUAL_SCAN_TAG = "manual-whatsapp-scan"
        private const val BACKGROUND_WORK_TAG = "persistent-whatsapp-guard"
        private const val MAX_RECENT_LOGS = 10
    }
}

class HomeViewModelFactory(private val appContext: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            "Unknown ViewModel class ${modelClass.name}"
        }
        val database = AppDatabase.getDatabase(appContext)
        val workManager = WorkManager.getInstance(appContext)
        return HomeViewModel(
            scanLogDao = database.scanLogDao(),
            workManager = workManager,
            appContext = appContext
        ) as T
    }
}
