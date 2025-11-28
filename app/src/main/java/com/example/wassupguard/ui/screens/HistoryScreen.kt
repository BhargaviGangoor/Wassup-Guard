package com.example.wassupguard.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wassupguard.data.entity.ScanLog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(historyViewModel: HistoryViewModel = viewModel()) {
    val scanLogs by historyViewModel.scanLogs.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan History") },
                actions = {
                    IconButton(onClick = { historyViewModel.clearHistory() }) {
                        Icon(Icons.Rounded.Delete, contentDescription = "Clear History")
                    }
                }
            )
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it).fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(scanLogs) { log ->
                HistoryItem(log = log)
            }
        }
    }
}

@Composable
fun HistoryItem(log: ScanLog) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(if (log.verdict == "Safe") Icons.Rounded.Shield else Icons.Rounded.Warning, contentDescription = null, tint = if (log.verdict == "Safe") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = log.fileName, fontWeight = FontWeight.SemiBold)
                Text(text = "${log.timestampEpochMillis}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text(text = log.hashSha256.take(16), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))

            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = log.verdict,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.small)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}
