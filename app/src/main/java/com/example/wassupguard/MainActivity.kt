package com.example.wassupguard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import android.os.Build
import android.Manifest
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.wassupguard.ui.theme.WassupGuardTheme
import com.example.wassupguard.util.Notifications

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Create notification channels
        Notifications.ensureChannels(this)

        // Schedule a one-time unique work to kick off initial scanning/setup
        val work = OneTimeWorkRequestBuilder<com.example.wassupguard.workers.FileMonitorWorker>()
            .build()
        WorkManager.getInstance(this)
            .enqueueUniqueWork("file-monitor-initial", ExistingWorkPolicy.KEEP, work)

        enableEdgeToEdge()
        setContent {
            WassupGuardTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var lastAction by remember { mutableStateOf("Ready") }

    // Ask for notification permission on Android 13+
    val notifPermissionLauncher = remember {
        (context as ComponentActivity).registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            lastAction = if (granted) "Notifications allowed" else "Notifications denied"
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp)) {
            Text(text = "Wassup Guard")
            Spacer(modifier = Modifier.height(12.dp))
            Button(onClick = {
                val request = OneTimeWorkRequestBuilder<com.example.wassupguard.workers.FileMonitorWorker>().build()
                WorkManager.getInstance(context).enqueue(request)
                lastAction = "Worker enqueued"
            }) {
                Text("Run test scan")
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "Status: $lastAction")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WassupGuardTheme {
        MainScreen()
    }
}