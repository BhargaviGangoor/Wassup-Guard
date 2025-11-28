package com.example.wassupguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(settingsViewModel: SettingsViewModel = viewModel()) {
    val uiState by settingsViewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
            )
        }
    ) {
        Column(modifier = Modifier.padding(it).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = uiState.apiKey,
                onValueChange = { settingsViewModel.onApiKeyChanged(it) },
                label = { Text("VirusTotal API Key") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Real-time Protection", modifier = Modifier.weight(1f))
                Switch(checked = uiState.isRealTimeProtectionEnabled, onCheckedChange = { settingsViewModel.onRealTimeProtectionChanged(it) })
            }

            if (uiState.isRealTimeProtectionEnabled) {
                Text("Simulated mode active", color = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { settingsViewModel.saveConfiguration() },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Save Configuration")
            }
        }
    }
}
