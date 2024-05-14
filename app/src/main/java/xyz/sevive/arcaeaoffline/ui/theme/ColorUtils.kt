package xyz.sevive.arcaeaoffline.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass

@Composable
fun ratingClassColor(ratingClass: ArcaeaScoreRatingClass): Color {
    return when (ratingClass) {
        ArcaeaScoreRatingClass.PAST -> ArcaeaDifficultyExtendedColors.current.past
        ArcaeaScoreRatingClass.PRESENT -> ArcaeaDifficultyExtendedColors.current.present
        ArcaeaScoreRatingClass.FUTURE -> ArcaeaDifficultyExtendedColors.current.future
        ArcaeaScoreRatingClass.BEYOND -> ArcaeaDifficultyExtendedColors.current.beyond
        ArcaeaScoreRatingClass.ETERNAL -> ArcaeaDifficultyExtendedColors.current.eternal
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
