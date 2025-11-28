package com.example.wassupguard.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.wassupguard.data.AppDatabase
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val scanLogDao = AppDatabase.getDatabase(application).scanLogDao()

    val scanLogs = scanLogDao.observeAll()

    fun clearHistory() {
        viewModelScope.launch {
            scanLogDao.deleteAll()
        }
    }
}