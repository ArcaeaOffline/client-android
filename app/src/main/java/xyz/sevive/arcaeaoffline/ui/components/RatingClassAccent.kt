package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.materialkolor.hct.Hct
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClassDisplay
import xyz.sevive.arcaeaoffline.ui.theme.isSystemInDarkTheme
import xyz.sevive.arcaeaoffline.ui.theme.ratingClassColor

/**
 * A rating class tint for a surface: a gradient that fades the class color in from the
 * trailing edge, and a text color legible on top of it.
 */
data class RatingClassAccent(
    val backgroundBrush: Brush,
    val textColor: Color,
)

/**
 * Resolves the [RatingClassAccent] of [display], animating the color when [display] changes.
 */
@Composable
fun rememberRatingClassAccent(display: ArcaeaRatingClassDisplay): RatingClassAccent {
    // TODO: change to local when theme settings added
    val isDarkTheme = isSystemInDarkTheme()
    val textTone = if (isDarkTheme) 80.0 else 32.0
    val accentColor by animateColorAsState(
        targetValue = ratingClassColor(display),
        label = "accentColor",
    )

    return remember(accentColor, isDarkTheme) {
        val hct = Hct.fromInt(accentColor.toArgb())
        val backgroundBrush =
            if (isDarkTheme) {
                val backgroundEnd = Color(hct.withTone(20.0).toInt())
                Brush.horizontalGradient(
                    0.48f to backgroundEnd.copy(alpha = 0f),
                    0.68f to backgroundEnd.copy(alpha = 0.12f),
                    0.84f to backgroundEnd.copy(alpha = 0.45f),
                    0.94f to backgroundEnd.copy(alpha = 0.78f),
                    1.00f to backgroundEnd.copy(alpha = 0.90f),
                )
            } else {
                Brush.horizontalGradient(
                    0.00f to accentColor.copy(alpha = 0f),
                    0.40f to accentColor.copy(alpha = 0f),
                    1.00f to accentColor.copy(alpha = 0.22f),
                )
            }

        RatingClassAccent(
            backgroundBrush = backgroundBrush,
            textColor = Color(hct.withTone(textTone).toInt()),
        )
    }
}
