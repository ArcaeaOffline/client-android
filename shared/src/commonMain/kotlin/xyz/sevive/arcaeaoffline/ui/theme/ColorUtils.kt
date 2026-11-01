package xyz.sevive.arcaeaoffline.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClassDisplay
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreGrade

@Composable
fun ratingClassColor(ratingClass: ArcaeaRatingClass): Color =
    when (ratingClass) {
        ArcaeaRatingClass.PAST -> LocalArcaeaColors.current.past
        ArcaeaRatingClass.PRESENT -> LocalArcaeaColors.current.present
        ArcaeaRatingClass.FUTURE -> LocalArcaeaColors.current.future
        ArcaeaRatingClass.BEYOND -> LocalArcaeaColors.current.beyond
        ArcaeaRatingClass.ETERNAL -> LocalArcaeaColors.current.eternal
    }

@Composable
fun ratingClassColor(display: ArcaeaRatingClassDisplay): Color =
    when (display) {
        ArcaeaRatingClassDisplay.INSCRIBED -> LocalArcaeaColors.current.inscribed
        else -> ratingClassColor(display.ratingClass)
    }

@Composable
fun playResultGradeGradientBrush(score: Int): Brush {
    val colors =
        when (ArcaeaScoreGrade.of(score)) {
            ArcaeaScoreGrade.EX_PLUS -> LocalArcaeaGradeGradientColors.current.exPlus
            ArcaeaScoreGrade.EX -> LocalArcaeaGradeGradientColors.current.ex
            ArcaeaScoreGrade.AA -> LocalArcaeaGradeGradientColors.current.aa
            ArcaeaScoreGrade.A -> LocalArcaeaGradeGradientColors.current.a
            ArcaeaScoreGrade.B -> LocalArcaeaGradeGradientColors.current.b
            ArcaeaScoreGrade.C -> LocalArcaeaGradeGradientColors.current.c
            ArcaeaScoreGrade.D -> LocalArcaeaGradeGradientColors.current.d
        }
    return Brush.verticalGradient(colors)
}
