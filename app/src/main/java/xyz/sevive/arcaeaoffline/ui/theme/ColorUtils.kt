package xyz.sevive.arcaeaoffline.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass

@Composable
fun ratingClassColor(ratingClass: ArcaeaRatingClass): Color {
    return when (ratingClass) {
        ArcaeaRatingClass.PAST -> LocalArcaeaColors.current.past
        ArcaeaRatingClass.PRESENT -> LocalArcaeaColors.current.present
        ArcaeaRatingClass.FUTURE -> LocalArcaeaColors.current.future
        ArcaeaRatingClass.BEYOND -> LocalArcaeaColors.current.beyond
        ArcaeaRatingClass.ETERNAL -> LocalArcaeaColors.current.eternal
    }
}

@Composable
fun playResultGradeGradientBrush(score: Int): Brush {
    val colors = when {
        score >= 9900000 -> LocalArcaeaGradeGradientColors.current.exPlus
        score >= 9800000 -> LocalArcaeaGradeGradientColors.current.ex
        score >= 9500000 -> LocalArcaeaGradeGradientColors.current.aa
        score >= 9200000 -> LocalArcaeaGradeGradientColors.current.a
        score >= 8900000 -> LocalArcaeaGradeGradientColors.current.b
        score >= 8600000 -> LocalArcaeaGradeGradientColors.current.c
        else -> LocalArcaeaGradeGradientColors.current.d
    }
    return Brush.verticalGradient(colors)
}
