package xyz.sevive.arcaeaoffline.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext

val LocalExtendedColorScheme = staticCompositionLocalOf { ExtendedColorScheme() }

@Composable
fun ArcaeaOfflineTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
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
