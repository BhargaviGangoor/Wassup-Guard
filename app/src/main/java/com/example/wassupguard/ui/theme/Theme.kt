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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WhatsAppAccent,
    onPrimary = Color.Black,
    primaryContainer = WhatsAppPrimary,
    onPrimaryContainer = Color.White,
    secondary = WhatsAppAccentDark,
    onSecondary = Color.Black,
    background = WhatsAppSurface,
    onBackground = Color(0xFFECEFF1),
    surface = WhatsAppSurfaceLight,
    onSurface = Color(0xFFECEFF1),
    surfaceVariant = WhatsAppCard,
    onSurfaceVariant = Color(0xFFB2CCD6),
    error = WhatsAppDanger
)

private val LightColorScheme = lightColorScheme(
    primary = WhatsAppPrimary,
    onPrimary = Color.White,
    primaryContainer = WhatsAppAccent,
    onPrimaryContainer = Color.Black,
    secondary = WhatsAppAccentDark,
    onSecondary = Color.White,
    background = Color(0xFFE5F7F1),
    onBackground = Color(0xFF041A15),
    surface = Color(0xFFF4FFFB),
    onSurface = Color(0xFF041A15),
    surfaceVariant = Color(0xFFE0F2F1),
    onSurfaceVariant = Color(0xFF11332D),
    error = WhatsAppDanger
)

@Composable
fun WassupGuardTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}