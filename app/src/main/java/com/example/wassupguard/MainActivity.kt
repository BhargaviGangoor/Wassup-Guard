package com.example.wassupguard

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.wassupguard.monitor.FileMonitorService
import com.example.wassupguard.ui.home.HomeRoute
import com.example.wassupguard.ui.home.HomeViewModel
import com.example.wassupguard.ui.home.HomeViewModelFactory
import com.example.wassupguard.ui.theme.WassupGuardTheme
import com.example.wassupguard.util.Notifications

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModels {
        HomeViewModelFactory(applicationContext)
    }

    private val manageAllFilesLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (hasAllFilesAccess()) {
                startFileMonitorService()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Notifications.ensureChannels(this)
        scheduleInitialScan()
        ensureAllFilesAccess()

        enableEdgeToEdge()
        setContent {
            WassupGuardTheme {
                HomeRoute(viewModel = homeViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (hasAllFilesAccess()) {
            startFileMonitorService()
        }
    }

    private fun ensureAllFilesAccess() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !hasAllFilesAccess()) {
            requestAllFilesPermission()
        } else {
            startFileMonitorService()
        }
    }

    private fun hasAllFilesAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            true
        }
    }

    private fun requestAllFilesPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) return
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
            data = Uri.parse("package:$packageName")
        }
        runCatching { manageAllFilesLauncher.launch(intent) }
            .onFailure {
                val fallback = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                manageAllFilesLauncher.launch(fallback)
            }
    }

    private fun startFileMonitorService() {
        ContextCompat.startForegroundService(
            this,
            Intent(this, FileMonitorService::class.java)
        )
    }

    private fun scheduleInitialScan() {
        val request = OneTimeWorkRequestBuilder<com.example.wassupguard.workers.FileMonitorWorker>()
            .build()
        WorkManager.getInstance(this)
            .enqueueUniqueWork("file-monitor-initial", ExistingWorkPolicy.KEEP, request)
    }
}