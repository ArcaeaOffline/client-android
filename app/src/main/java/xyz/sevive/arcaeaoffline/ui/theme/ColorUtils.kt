package xyz.sevive.arcaeaoffline.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass

@Composable
fun ratingClassColor(ratingClass: ArcaeaRatingClass): Color {
    return when (ratingClass) {
        ArcaeaRatingClass.PAST -> ArcaeaDifficultyExtendedColors.current.past
        ArcaeaRatingClass.PRESENT -> ArcaeaDifficultyExtendedColors.current.present
        ArcaeaRatingClass.FUTURE -> ArcaeaDifficultyExtendedColors.current.future
        ArcaeaRatingClass.BEYOND -> ArcaeaDifficultyExtendedColors.current.beyond
        ArcaeaRatingClass.ETERNAL -> ArcaeaDifficultyExtendedColors.current.eternal
    }
}

@Composable
fun scoreGradientBrush(score: Int): Brush {
    val colors = when {
        score >= 9900000 -> ArcaeaGradeGradientExtendedColors.current.exPlus
        score >= 9800000 -> ArcaeaGradeGradientExtendedColors.current.ex
        score >= 9500000 -> ArcaeaGradeGradientExtendedColors.current.aa
        score >= 9200000 -> ArcaeaGradeGradientExtendedColors.current.a
        score >= 8900000 -> ArcaeaGradeGradientExtendedColors.current.b
        score >= 8600000 -> ArcaeaGradeGradientExtendedColors.current.c
        else -> ArcaeaGradeGradientExtendedColors.current.d
    }
    return Brush.verticalGradient(colors)
}
