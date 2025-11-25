package com.example.wassupguard.ui.home

import android.Manifest
import android.os.Build
import android.text.format.DateFormat
import android.text.format.DateUtils
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wassupguard.data.entity.ScanLog
import kotlinx.coroutines.launch
import java.util.Locale
import com.example.wassupguard.util.safety.SafetyTip

@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.statusMessage) {
        if (uiState.statusMessage.isNotBlank()) {
            scope.launch {
                snackbarHostState.showSnackbar(uiState.statusMessage)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        HomeScreen(
            state = uiState,
            onScanNow = { viewModel.runQuickScan() },
            onBackgroundToggle = { viewModel.toggleBackgroundProtection(it) },
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        )
    }
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    onScanNow: () -> Unit,
    onBackgroundToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val notificationLauncher = rememberNotificationPermission()

    LaunchedEffect(Unit) {
        notificationLauncher()
    }

    LazyColumn(
        modifier = modifier
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeroCard(
                lastScanTimestamp = state.lastScanTimestamp,
                threatsDetected = state.threatsDetected,
                totalScans = state.totalScans
            )
        }
        item {
            GuardActions(
                isScheduling = state.isSchedulingScan,
                backgroundProtectionEnabled = state.backgroundProtectionEnabled,
                onScanNow = onScanNow,
                onBackgroundToggle = onBackgroundToggle
            )
        }
        item {
            ThreatStatsRow(
                threatsDetected = state.threatsDetected,
                totalScans = state.totalScans
            )
        }
        item {
            SafetyTipsSection(tips = state.safetyTips)
        }
        item {
            RecentScansSection(scanLogs = state.scanLogs)
        }
    }
}

@Composable
private fun rememberNotificationPermission(): () -> Unit {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        var asked by remember { mutableStateOf(false) }
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { asked = true }
        )
        {
            if (!asked) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    } else {
        { }
    }
}

@Composable
private fun HeroCard(
    lastScanTimestamp: Long?,
    threatsDetected: Int,
    totalScans: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Wassup Guard",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = friendlyLastScan(lastScanTimestamp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    )
                }
                Box(
                    modifier = Modifier
                        .size(72.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f),
                            startAngle = -90f,
                            sweepAngle = 360f,
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 10.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                        val sweep = if (totalScans == 0) 0f else (threatsDetected / totalScans.toFloat()) * 360f
                        drawArc(
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            startAngle = -90f,
                            sweepAngle = sweep.coerceIn(0f, 360f),
                            useCenter = false,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 10.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun GuardActions(
    isScheduling: Boolean,
    backgroundProtectionEnabled: Boolean,
    onScanNow: () -> Unit,
    onBackgroundToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onScanNow,
                enabled = !isScheduling,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(text = if (isScheduling) "Scheduling..." else "Scan WhatsApp Now")
            }
            Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Background Guard",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Auto-scans chats every few hours",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                Switch(
                    checked = backgroundProtectionEnabled,
                    onCheckedChange = onBackgroundToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                )
            }
        }
    }
}

@Composable
private fun ThreatStatsRow(
    threatsDetected: Int,
    totalScans: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            title = "Threats blocked",
            value = threatsDetected.toString(),
            icon = Icons.Rounded.Warning,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            iconTint = MaterialTheme.colorScheme.error
        )
        StatCard(
            title = "Files scanned",
            value = totalScans.toString(),
            icon = Icons.Rounded.History,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            iconTint = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .weight(1f),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
private fun RecentScansSection(
    scanLogs: List<ScanLog>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Recent scans",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            if (scanLogs.isEmpty()) {
                EmptyState()
            } else {
                scanLogs.forEachIndexed { index, log ->
                    ScanLogRow(log)
                    if (index != scanLogs.lastIndex) {
                        Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SafetyTipsSection(
    tips: List<SafetyTip>,
    modifier: Modifier = Modifier
) {
    if (tips.isEmpty()) return
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "WhatsApp safety tips",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            tips.forEach { tip ->
                SafetyTipRow(tip = tip)
            }
        }
    }
}

@Composable
private fun SafetyTipRow(tip: SafetyTip) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Rounded.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = tip.title,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                text = tip.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Rounded.BugReport,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(48.dp)
        )
        Text(
            text = "No scans yet",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "Tap \"Scan WhatsApp Now\" to run your first check.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun ScanLogRow(log: ScanLog) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = log.fileName,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${formatFileSize(log.fileSizeBytes)} • ${formatTimestamp(log.timestampEpochMillis)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        VerdictPill(verdict = log.verdict)
    }
}

@Composable
private fun VerdictPill(verdict: String) {
    val (bg, fg, icon) = verdictColors(verdict)
    Row(
        modifier = Modifier
            .background(bg, shape = CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
        Text(
            text = verdict,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = fg
        )
    }
}

@Composable
private fun verdictColors(verdict: String): Triple<Color, Color, androidx.compose.ui.graphics.vector.ImageVector> {
    val lower = verdict.lowercase(Locale.getDefault())
    return when {
        lower.contains("malicious") -> Triple(
            MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
            MaterialTheme.colorScheme.error,
            Icons.Rounded.BugReport
        )
        lower.contains("suspicious") -> Triple(
            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.tertiary,
            Icons.Rounded.Warning
        )
        else -> Triple(
            MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
            MaterialTheme.colorScheme.primary,
            Icons.Rounded.Shield
        )
    }
}

private fun friendlyLastScan(timestamp: Long?): String {
    if (timestamp == null) return "No scans yet"
    val relative = DateUtils.getRelativeTimeSpanString(
        timestamp,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    )
    return "Last scan $relative"
}

private fun formatTimestamp(timestamp: Long): String {
    return DateFormat.format("MMM d, h:mm a", timestamp).toString()
}

private fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val kb = bytes / 1024.0
    if (kb < 1024) return "${kb.toInt()} KB"
    val mb = kb / 1024.0
    return String.format(Locale.getDefault(), "%.1f MB", mb)
}

