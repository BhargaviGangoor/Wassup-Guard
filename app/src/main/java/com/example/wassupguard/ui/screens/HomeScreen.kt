package com.example.wassupguard.ui.screens

import android.app.Application
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.wassupguard.data.entity.ScanLog
import com.example.wassupguard.ui.home.HomeViewModel
import com.example.wassupguard.ui.home.HomeViewModelFactory

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(LocalContext.current.applicationContext as Application)
    )
) {
    val uiState by homeViewModel.uiState.collectAsState()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
        onResult = { uri ->
            if (uri != null) {
                homeViewModel.onDirectorySelected(uri.toString())
            }
        }
    )

    val totalScans = uiState.scanLogs.size
    val threatsDetected = uiState.scanLogs.count { it.verdict.contains("malicious", ignoreCase = true) }
    val cleanFiles = totalScans - threatsDetected
    val score = if (totalScans > 0) cleanFiles.toFloat() / totalScans.toFloat() else 1f

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Blurred green glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .blur(100.dp)
                .alpha(0.3f)
                .background(
                    Brush.radialGradient(
                        colors = listOf(MaterialTheme.colorScheme.primary, Color.Transparent),
                        center = Offset(0f, 0f),
                        radius = 400f
                    )
                )
        )
        if (uiState.isFolderSelectionRequired) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Rounded.FolderOpen, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Select Your WhatsApp Folder",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "To get started, please grant access to your WhatsApp media folder. This allows Wassup Guard to scan for threats.",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { launcher.launch(null) },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    Text("Select Folder")
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Wassup Guard",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusIndicator(status = uiState.statusMessage)
                }

                item {
                    RadialScore(score = score)
                }

                item {
                    Button(
                        onClick = { homeViewModel.runQuickScan() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Rounded.PlayArrow, contentDescription = "Scan File")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Run Quick Scan", fontSize = 16.sp)
                    }
                }

                item {
                    OutlinedButton(
                        onClick = { launcher.launch(null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Rounded.FolderOpen, contentDescription = "Change Folder")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Change Scan Folder", fontSize = 16.sp)
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        SummaryCard(title = "Files Clean", value = cleanFiles.toString(), modifier = Modifier.weight(1f))
                        SummaryCard(title = "Threats Blocked", value = threatsDetected.toString(), modifier = Modifier.weight(1f))
                    }
                }

                item {
                    Text(
                        text = "Recent Scans",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                items(uiState.scanLogs) {
                    ScanLogItem(scanLog = it)
                }
            }
        }
    }
}

@Composable
fun StatusIndicator(status: String) {
    // TODO: Add pulsing animation
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier
            .size(8.dp)
            .background(MaterialTheme.colorScheme.primary, CircleShape))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = status, color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f), fontSize = 14.sp)
    }
}

@Composable
fun RadialScore(score: Float) {
    val animatedScore by animateFloatAsState(targetValue = score, animationSpec = tween(durationMillis = 1000))
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val primary = MaterialTheme.colorScheme.primary
    val onBackground = MaterialTheme.colorScheme.onBackground

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawArc(
                color = surfaceVariant,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = primary,
                startAngle = -90f,
                sweepAngle = 360 * animatedScore,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Text(text = "${(score * 100).toInt()}%", fontSize = 48.sp, fontWeight = FontWeight.Bold, color = onBackground)
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ScanLogItem(scanLog: ScanLog) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            val isSafe = scanLog.verdict == "Safe"
            Icon(if (isSafe) Icons.Rounded.Check else Icons.Rounded.Close, contentDescription = null, tint = if (isSafe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = scanLog.fileName, fontWeight = FontWeight.SemiBold)
                Text(text = scanLog.verdict, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
        }
    }
}
