package com.openjarvis.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * "Void" color palette used throughout the OpenJarvis UI
 * (dashboard, settings, onboarding, floating overlay, etc).
 */
object VoidColor {
    val Void = Color(0xFF0A0A0F)
    val Void600 = Color(0xFF52525B)
    val Void700 = Color(0xFF3F3F46)
    val Void800 = Color(0xFF27272A)
    val Void900 = Color(0xFF18181B)
    val Void950 = Color(0xFF09090B)
    val Violet = Color(0xFF8B5CF6)
    val VioletDim = Color(0xFF4C3575)
    val Cyan = Color(0xFF22D3EE)
    val Amber = Color(0xFFF59E0B)
    val Green = Color(0xFF22C55E)
    val Red = Color(0xFFEF4444)
    val TextPrimary = Color(0xFFF5F5F7)
    val TextSecondary = Color(0xFFA1A1AA)
    val TextDisabled = Color(0xFF52525B)
    val BorderSubtle = Color(0xFF27272A)
    val BorderGlow = Color(0xFF8B5CF6)
}

private val OpenJarvisColorScheme = darkColorScheme(
    primary = VoidColor.Violet,
    onPrimary = VoidColor.TextPrimary,
    secondary = VoidColor.Cyan,
    onSecondary = VoidColor.Void,
    background = VoidColor.Void,
    onBackground = VoidColor.TextPrimary,
    surface = VoidColor.Void,
    onSurface = VoidColor.TextPrimary,
    error = VoidColor.Red,
    onError = VoidColor.TextPrimary,
    outline = VoidColor.BorderSubtle
)

@Composable
fun OpenJarvisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OpenJarvisColorScheme,
        content = content
    )
}
