package com.example.babyfoot.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = Emerald,
    onPrimary = Midnight,
    primaryContainer = Indigo,
    onPrimaryContainer = Cloud,
    secondary = Gold,
    onSecondary = Midnight,
    secondaryContainer = Cloud,
    onSecondaryContainer = Midnight,
    surface = Cloud,
    onSurface = Midnight,
    background = Cloud,
    onBackground = Midnight,
    error = Flame,
    onError = Cloud,
)

private val DarkColors = darkColorScheme(
    primary = Emerald,
    onPrimary = Midnight,
    primaryContainer = Indigo,
    onPrimaryContainer = Cloud,
    secondary = Gold,
    onSecondary = Midnight,
    secondaryContainer = Midnight,
    onSecondaryContainer = Cloud,
    surface = Midnight,
    onSurface = Cloud,
    background = Midnight,
    onBackground = Cloud,
    error = Flame,
    onError = Midnight,
)

@Composable
fun BabyfootTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}
