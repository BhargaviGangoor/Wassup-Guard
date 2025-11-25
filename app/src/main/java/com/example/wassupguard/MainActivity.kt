package com.example.wassupguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.wassupguard.ui.home.HomeRoute
import com.example.wassupguard.ui.home.HomeViewModel
import com.example.wassupguard.ui.home.HomeViewModelFactory
import com.example.wassupguard.ui.theme.WassupGuardTheme
import com.example.wassupguard.util.Notifications

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Notifications.ensureChannels(this)
        scheduleInitialScan()

        enableEdgeToEdge()
        setContent {
            WassupGuardTheme {
                HomeRoute(viewModel = homeViewModel)
            }
        }
    }

    private fun scheduleInitialScan() {
        val request = OneTimeWorkRequestBuilder<com.example.wassupguard.workers.FileMonitorWorker>()
            .build()
        WorkManager.getInstance(this)
            .enqueueUniqueWork("file-monitor-initial", ExistingWorkPolicy.KEEP, request)
    }
}