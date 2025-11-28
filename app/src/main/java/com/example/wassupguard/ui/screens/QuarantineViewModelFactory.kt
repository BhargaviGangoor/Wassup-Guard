package com.example.wassupguard.ui.screens

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class QuarantineViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuarantineViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuarantineViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}