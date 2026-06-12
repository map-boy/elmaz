package com.nyumbahub.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary          = NavyPrimary,
    onPrimary        = SurfaceLight,
    primaryContainer = NavyLight,
    secondary        = OrangeAccent,
    onSecondary      = SurfaceLight,
    background       = BackgroundLight,
    onBackground     = TextPrimary,
    surface          = SurfaceLight,
    onSurface        = TextPrimary,
    error            = ErrorRed,
    onError          = SurfaceLight
)

private val DarkColors = darkColorScheme(
    primary          = NavyLight,
    onPrimary        = SurfaceLight,
    primaryContainer = NavyDark,
    secondary        = OrangeAccent,
    onSecondary      = SurfaceLight,
    background       = BackgroundDark,
    onBackground     = SurfaceLight,
    surface          = SurfaceDark,
    onSurface        = SurfaceLight,
    error            = ErrorRed,
    onError          = SurfaceLight
)

@Composable
fun NyumbaHubTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        typography  = NyumbaTypography,
        content     = content
    )
}
