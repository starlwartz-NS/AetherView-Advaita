package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val AerospaceColorScheme = darkColorScheme(
    primary = AeroCyan,
    onPrimary = AeroBgMain,
    primaryContainer = AeroBgCardElevated,
    onPrimaryContainer = AeroCyan,
    secondary = AeroElectricBlue,
    onSecondary = AeroTextPrimary,
    secondaryContainer = AeroBgCard,
    onSecondaryContainer = AeroTextPrimary,
    background = AeroBgMain,
    onBackground = AeroTextPrimary,
    surface = AeroBgSecondary,
    onSurface = AeroTextPrimary,
    surfaceVariant = AeroBgCard,
    onSurfaceVariant = AeroTextSecondary,
    error = AeroCritical,
    onError = AeroTextPrimary,
    outline = AeroBorder,
    outlineVariant = AeroBorderGlow
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Aerospace dark mode by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AerospaceColorScheme,
        typography = AeroTypography,
        content = content
    )
}
