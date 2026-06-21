package com.mrcoder20.portx.presentation.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.mrcoder20.portx.domain.SettingsManager
import org.koin.compose.koinInject

@Composable
fun PortXTheme(
    settingsManager: SettingsManager = koinInject(),
    content: @Composable () -> Unit
) {
    val settingsState by settingsManager.settings.collectAsState()
    val accent = settingsState.accentColor
    val isDark = settingsState.theme == "DARK"

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = accent,
            secondary = accent.copy(alpha = 0.7f),
            background = BackgroundDark,
            surface = SurfaceDark,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color.White,
            onSurface = Color.White
        )
    } else {
        lightColorScheme(
            primary = accent,
            secondary = accent.copy(alpha = 0.7f),
            background = BackgroundLight,
            surface = SurfaceLight,
            onPrimary = Color.Black,
            onSecondary = Color.Black,
            onBackground = Color(0xFF1A1A1A),
            onSurface = Color(0xFF1A1A1A)
        )
    }

    // Update global neon colors for the rest of the app to use
    CompositionLocalProvider(
        LocalAccentColor provides accent,
        LocalAppSettings provides settingsState
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}

val LocalAccentColor = staticCompositionLocalOf { Color(0xFF00D1FF) }
val LocalAppSettings = staticCompositionLocalOf { com.mrcoder20.portx.domain.AppSettings() }

