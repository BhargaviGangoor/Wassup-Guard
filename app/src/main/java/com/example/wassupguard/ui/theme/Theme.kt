package com.example.wassupguard.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Green400,
    onPrimary = Slate800,
    primaryContainer = Green500,
    onPrimaryContainer = Slate800,
    secondary = Slate300,
    onSecondary = Slate800,
    background = Slate800,
    onBackground = Slate300,
    surface = Slate700,
    onSurface = Slate300,
    surfaceVariant = Slate500,
    onSurfaceVariant = Slate300,
    error = Red400,
    onError = Slate800
)

@Composable
fun WassupGuardTheme(
    darkTheme: Boolean = true, // Force dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}