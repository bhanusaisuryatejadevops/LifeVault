package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = VaultNavyDark,
    onPrimary = VaultOnNavyDark,
    primaryContainer = VaultNavyContainerDark,
    onPrimaryContainer = VaultOnNavyContainerDark,
    secondary = VaultTealDark,
    onSecondary = VaultOnTealDark,
    secondaryContainer = VaultTealContainerDark,
    onSecondaryContainer = VaultOnTealContainerDark,
    tertiary = VaultAmberDark,
    onTertiary = VaultOnAmberDark,
    tertiaryContainer = VaultAmberContainerDark,
    onTertiaryContainer = VaultOnAmberContainerDark,
    background = VaultBackgroundDark,
    surface = VaultSurfaceDark,
    surfaceVariant = VaultSurfaceVariantDark,
    onBackground = VaultOnBackgroundDark,
    onSurface = VaultOnSurfaceDark,
    onSurfaceVariant = VaultOnSurfaceVariantDark,
    outline = VaultOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = VaultNavyLight,
    onPrimary = VaultOnNavyLight,
    primaryContainer = VaultNavyContainerLight,
    onPrimaryContainer = VaultOnNavyContainerLight,
    secondary = VaultTealLight,
    onSecondary = VaultOnTealLight,
    secondaryContainer = VaultTealContainerLight,
    onSecondaryContainer = VaultOnTealContainerLight,
    tertiary = VaultAmberLight,
    onTertiary = VaultOnAmberLight,
    tertiaryContainer = VaultAmberContainerLight,
    onTertiaryContainer = VaultOnAmberContainerLight,
    background = VaultBackgroundLight,
    surface = VaultSurfaceLight,
    surfaceVariant = VaultSurfaceVariantLight,
    onBackground = VaultOnBackgroundLight,
    onSurface = VaultOnSurfaceLight,
    onSurfaceVariant = VaultOnSurfaceVariantLight,
    outline = VaultOutlineLight
)

@Composable
fun LifeVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set default to false to showcase polished custom palette
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward-compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    LifeVaultTheme(darkTheme = darkTheme, dynamicColor = dynamicColor, content = content)
}
