package com.example.wassupguard.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wassupguard.data.AppDatabase
import com.example.wassupguard.data.entity.ScanLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

data class HomeUiState(
    val status: String = "Monitoring WhatsApp",
    val safeScore: Float = 0.0f,
    val filesClean: Int = 0,
    val threatsBlocked: Int = 0,
    val recentScans: List<ScanLog> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val scanLogDao = AppDatabase.getDatabase(application).scanLogDao()

    init {
        viewModelScope.launch {
            scanLogDao.observeAll().collect { logs ->
                val filesClean = logs.count { it.verdict == "Safe" }
                val threatsBlocked = logs.size - filesClean
                val safeScore = if (logs.isEmpty()) 0f else filesClean.toFloat() / logs.size

                _uiState.value = HomeUiState(
                    filesClean = filesClean,
                    threatsBlocked = threatsBlocked,
                    safeScore = safeScore,
                    recentScans = logs.take(3)
                )
            }
        }
    }

    fun startScan() {
        // TODO: Implement scan logic
    }
}