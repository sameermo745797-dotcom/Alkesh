package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RudraColorScheme = darkColorScheme(
    primary = ArcCyan,
    onPrimary = Color(0xFF001F28),
    primaryContainer = Color(0xFF003644),
    onPrimaryContainer = ArcCyanBright,
    secondary = ArcGold,
    onSecondary = Color(0xFF281800),
    secondaryContainer = Color(0xFF442B00),
    onSecondaryContainer = ArcGoldBright,
    tertiary = HudGreenOnline,
    onTertiary = Color(0xFF002111),
    background = HudBackground,
    onBackground = TextPrimary,
    surface = HudSurface,
    onSurface = TextPrimary,
    surfaceVariant = HudSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = ArcCyanDim,
    error = HudRedAlert,
    onError = Color.White
)

@Composable
fun RudraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RudraColorScheme,
        typography = RudraTypography,
        content = content
    )
}
