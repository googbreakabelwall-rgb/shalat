package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Emerald,
    secondary = Gold,
    primaryContainer = EmeraldDim,
    secondaryContainer = DarkSurfaceSecondary,
    background = DarkBg,
    surface = DarkSurface,
    onPrimary = Color.White,
    onSecondary = DarkBg,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    outline = BorderColor
)

private val LightColorScheme = lightColorScheme(
    primary = Emerald,
    secondary = Gold,
    primaryContainer = Color(0xFFE8F5E9),
    secondaryContainer = Color(0xFFF1F8E9),
    background = Color(0xFFFAFAFA),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color(0xFF1A1A1A),
    onBackground = Color(0xFF0F1A13),
    onSurface = Color(0xFF0F1A13),
    outline = Color(0xFFE0E0E0)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Set darkTheme-first default for night comfort
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
