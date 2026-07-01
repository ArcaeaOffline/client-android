package xyz.sevive.arcaeaoffline.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun isSystemInDarkTheme(): Boolean = androidx.compose.foundation.isSystemInDarkTheme()

@Composable
actual fun ArcaeaOfflineTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable (() -> Unit),
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> {
                darkScheme
            }

            else -> {
                lightScheme
            }
        }

    CompositionLocalProvider(
        LocalArcaeaColors provides if (darkTheme) ArcaeaColors.Dark else ArcaeaColors.Light,
        LocalArcaeaGradeGradientColors provides if (darkTheme) ArcaeaGradeGradientColors.Dark else ArcaeaGradeGradientColors.Light,
        LocalExtendedColorScheme provides if (darkTheme) extendedDark else extendedLight,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content,
        )
    }
}
