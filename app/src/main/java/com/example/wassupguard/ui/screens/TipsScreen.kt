package com.example.wassupguard.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class SafetyTip(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val severity: Color
)

val tips = listOf(
    SafetyTip("Enable 2-Step Verification", "Add an extra layer of security to your account.", Icons.Rounded.Lock, Color.Red),
    SafetyTip("Review Privacy Settings", "Control who can see your information.", Icons.Rounded.Settings, Color.Yellow),
    SafetyTip("Be Wary of Strangers", "Don't share personal information with unknown contacts.", Icons.Rounded.Info, Color.Yellow),
    SafetyTip("Check for Scams", "Look out for suspicious links and messages.", Icons.Rounded.Info, Color.Red)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Safety Tips") },
            )
        }
    ) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(it).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tips) { tip ->
                TipCard(tip = tip)
            }
        }
    }
}

@Composable
fun TipCard(tip: SafetyTip) {
    Card(modifier = Modifier.aspectRatio(1f), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(tip.icon, contentDescription = null, tint = tip.severity)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = tip.title, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = tip.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
    }
}
