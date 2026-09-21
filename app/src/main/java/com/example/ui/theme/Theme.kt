package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TarhiNooColorScheme = darkColorScheme(
    primary = PrimaryGold,
    onPrimary = BgDark,
    primaryContainer = BrandGreen,
    onPrimaryContainer = SoftGold,
    secondary = SoftGold,
    onSecondary = BgDark,
    secondaryContainer = SurfaceCard,
    onSecondaryContainer = TextPrimary,
    tertiary = BrandGreenLight,
    onTertiary = TextPrimary,
    background = BgDark,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = SurfaceCardBorder,
    outlineVariant = SurfaceGlassBorder,
    error = ErrorRed,
    onError = TextPrimary
)

@Composable
fun TarhiNooTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TarhiNooColorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    TarhiNooTheme(content = content)
}
