package xyz.sevive.arcaeaoffline.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreRatingClass

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
