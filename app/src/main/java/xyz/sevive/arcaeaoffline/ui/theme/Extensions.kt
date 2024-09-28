package xyz.sevive.arcaeaoffline.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable

val MaterialTheme.extendedColorScheme
    @Composable @ReadOnlyComposable get() = LocalExtendedColorScheme.current

val MaterialTheme.arcaeaColors
    @Composable @ReadOnlyComposable get() = LocalArcaeaColors.current
