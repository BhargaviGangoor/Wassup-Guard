package com.example.wassupguard.ui.screens

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wassupguard.data.entity.ScanLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuarantineScreen(quarantineViewModel: QuarantineViewModel = viewModel(factory = QuarantineViewModelFactory(LocalContext.current.applicationContext as Application))) {
    val quarantinedFiles by quarantineViewModel.quarantinedFiles.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quarantine") },
            )
        }
    ) {
        if (quarantinedFiles.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(it), contentAlignment = Alignment.Center) {
                Text(text = "You are safe!")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(it).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(quarantinedFiles) { file ->
                    QuarantineItem(
                        file = file, 
                        onDelete = { quarantineViewModel.deleteFile(file) },
                        onRestore = { quarantineViewModel.restoreAndOpenFile(file) }
                    )
                }
            }
        }
    }
}

@Composable
fun QuarantineItem(file: ScanLog, onDelete: () -> Unit, onRestore: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onErrorContainer)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = file.fileName, fontWeight = FontWeight.SemiBold)
                Text(text = file.hashSha256.take(16), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f))
            }
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(onClick = onRestore) {
                Icon(Icons.AutoMirrored.Rounded.OpenInNew, contentDescription = "Restore and Open", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Rounded.Delete, contentDescription = "Delete Permanently", tint = MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}
