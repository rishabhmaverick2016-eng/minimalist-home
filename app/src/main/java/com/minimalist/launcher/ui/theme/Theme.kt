package com.minimalist.launcher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class MinimalistColors(
    val background: Color,
    val surface: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val accent: Color,
    val divider: Color
)

fun getThemePalette(themeName: String): MinimalistColors {
    return when (themeName) {
        "LIGHT_CLEAN" -> MinimalistColors(
            background = LightBackground,
            surface = LightSurface,
            textPrimary = LightTextPrimary,
            textSecondary = LightTextSecondary,
            accent = LightAccent,
            divider = LightDivider
        )
        "WARM_PAPER" -> MinimalistColors(
            background = WarmBackground,
            surface = WarmSurface,
            textPrimary = WarmTextPrimary,
            textSecondary = WarmTextSecondary,
            accent = WarmAccent,
            divider = WarmDivider
        )
        else -> MinimalistColors( // OLED_BLACK default
            background = OledBackground,
            surface = OledSurface,
            textPrimary = OledTextPrimary,
            textSecondary = OledTextSecondary,
            accent = OledAccent,
            divider = OledDivider
        )
    }
}

@Composable
fun MinimalistLauncherTheme(
    themeName: String = "OLED_BLACK",
    content: @Composable () -> Unit
) {
    val palette = getThemePalette(themeName)
    val isDark = themeName != "LIGHT_CLEAN" && themeName != "WARM_PAPER"

    val colorScheme = if (isDark) {
        darkColorScheme(
            background = palette.background,
            surface = palette.surface,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary
        )
    } else {
        lightColorScheme(
            background = palette.background,
            surface = palette.surface,
            onBackground = palette.textPrimary,
            onSurface = palette.textPrimary
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
