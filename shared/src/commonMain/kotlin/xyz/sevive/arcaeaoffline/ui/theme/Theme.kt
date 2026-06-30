package xyz.sevive.arcaeaoffline.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalExtendedColorScheme = staticCompositionLocalOf { ExtendedColorScheme() }

@Composable
expect fun isSystemInDarkTheme(): Boolean

@Composable
expect fun ArcaeaOfflineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
)
