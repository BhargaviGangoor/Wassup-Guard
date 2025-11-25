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
    val safetyTips: List<SafetyTip> = emptyList()
)

class HomeViewModel(
    private val scanLogDao: ScanLogDao,
    private val workManager: WorkManager,
    private val tipsProvider: SafetyTipsProvider = SafetyTipsProvider()
) : ViewModel() {

    private val statusMessage = MutableStateFlow("Ready")
    private val backgroundProtection = MutableStateFlow(false)
    private val scheduling = MutableStateFlow(false)
    private val tipsFlow = MutableStateFlow(tipsProvider.tipsOfTheDay())

    val uiState: StateFlow<HomeUiState> = combine(
        scanLogDao.observeAll(),
        statusMessage,
        backgroundProtection,
        scheduling,
        tipsFlow
    ) { logs, status, backgroundEnabled, schedulingScan, tips ->
        HomeUiState(
            scanLogs = logs.take(MAX_RECENT_LOGS),
            totalScans = logs.size,
            threatsDetected = logs.count { it.verdict.contains("malicious", ignoreCase = true) },
            lastScanTimestamp = logs.firstOrNull()?.timestampEpochMillis,
            statusMessage = status,
            backgroundProtectionEnabled = backgroundEnabled,
            isSchedulingScan = schedulingScan,
            safetyTips = tips
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState()
    )

    fun runQuickScan() {
        viewModelScope.launch {
            scheduling.value = true
            val request = OneTimeWorkRequestBuilder<FileMonitorWorker>()
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
            backgroundProtection.value = enable
            if (enable) {
                val periodicRequest = PeriodicWorkRequestBuilder<FileMonitorWorker>(
                    6, TimeUnit.HOURS
                ).addTag(BACKGROUND_WORK_TAG).build()
                workManager.enqueueUniquePeriodicWork(
                    BACKGROUND_WORK_TAG,
                    ExistingPeriodicWorkPolicy.UPDATE,
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
            workManager = workManager
        ) as T
    }
}

