package xyz.sevive.arcaeaoffline.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.jthemedetecor.OsThemeDetector
import java.util.function.Consumer

/**
 * https://github.com/zacharee/MultiplatformMaterialYou/blob/de1b4f7e563ddc9df53d26235d312009ee0d8aa8/library/src/jvmMain/kotlin/dev/zwander/compose/ThemeInfo.jvm.kt#L22-L49
 *
 * MIT License, Copyright (c) 2024 Zachary Wander
 */
@Composable
actual fun isSystemInDarkTheme(): Boolean {
    val (osThemeDetector, isSupported) =
        remember {
            OsThemeDetector.getDetector() to OsThemeDetector.isSupported()
        }

    var dark by remember {
        mutableStateOf(isSupported && osThemeDetector.isDark)
    }

    DisposableEffect(osThemeDetector, isSupported) {
        val listener =
            Consumer { darkMode: Boolean ->
                dark = darkMode
            }

        if (isSupported) {
            osThemeDetector.registerListener(listener)
        }

        onDispose {
            if (isSupported) {
                osThemeDetector.removeListener(listener)
            }
        }
    }

    return dark
}

@Composable
actual fun ArcaeaOfflineTheme(
    darkTheme: Boolean,
    dynamicColor: Boolean,
    content: @Composable (() -> Unit),
) {
    CompositionLocalProvider(
        LocalArcaeaColors provides if (darkTheme) ArcaeaColors.Dark else ArcaeaColors.Light,
        LocalArcaeaGradeGradientColors provides if (darkTheme) ArcaeaGradeGradientColors.Dark else ArcaeaGradeGradientColors.Light,
        LocalExtendedColorScheme provides if (darkTheme) extendedDark else extendedLight,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) darkScheme else lightScheme,
            typography = Typography,
            content = content,
        )
    }
}
