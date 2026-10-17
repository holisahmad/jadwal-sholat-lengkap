package com.jadwalsholat.lengkap.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun JadwalSholatLengkapTheme(
    themeMode: String = "dark", // "system", "dark", "light"
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode.lowercase()) {
        "light" -> false
        "system" -> isSystemDark
        else -> true // default "dark"
    }

    val nurColors = if (isDark) DarkNurColors else LightNurColors

    val colorScheme = if (isDark) {
        darkColorScheme(
            primary = nurColors.primary,
            onPrimary = nurColors.primaryFg,
            primaryContainer = nurColors.raised,
            onPrimaryContainer = nurColors.fg,
            secondary = nurColors.gold,
            onSecondary = nurColors.primaryFg,
            background = nurColors.bg,
            onBackground = nurColors.fg,
            surface = nurColors.surface,
            onSurface = nurColors.fg,
            surfaceVariant = nurColors.raised,
            onSurfaceVariant = nurColors.muted,
            outline = nurColors.border,
            outlineVariant = nurColors.border.copy(alpha = 0.6f)
        )
    } else {
        lightColorScheme(
            primary = nurColors.primary,
            onPrimary = nurColors.primaryFg,
            primaryContainer = nurColors.raised,
            onPrimaryContainer = nurColors.fg,
            secondary = nurColors.gold,
            onSecondary = nurColors.primaryFg,
            background = nurColors.bg,
            onBackground = nurColors.fg,
            surface = nurColors.surface,
            onSurface = nurColors.fg,
            surfaceVariant = nurColors.raised,
            onSurfaceVariant = nurColors.muted,
            outline = nurColors.border,
            outlineVariant = nurColors.border.copy(alpha = 0.6f)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = nurColors.bg.toArgb()
            window.navigationBarColor = nurColors.surface.toArgb()
            // In light mode, status bar icons should be dark (true), in dark mode white (false)
            val insets = WindowCompat.getInsetsController(window, view)
            insets.isAppearanceLightStatusBars = !isDark
            insets.isAppearanceLightNavigationBars = !isDark
        }
    }

    CompositionLocalProvider(LocalNurColors provides nurColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
