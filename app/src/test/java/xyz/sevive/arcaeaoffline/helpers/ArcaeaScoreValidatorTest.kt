package xyz.sevive.arcaeaoffline.helpers

import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.Score

class ArcaeaScoreValidatorTest {
    @Test
    fun maxRecallOverflow() {
        val chartInfo = ChartInfo(
            songId = "test",
            ratingClass = ArcaeaScoreRatingClass.FUTURE,
            constant = 95,
            notes = 920,
        )

        val score = Score(
            id = 0,
            songId = "test",
            ratingClass = ArcaeaScoreRatingClass.FUTURE,
            score = 9984560,
            pure = 917,
            far = 3,
            lost = 0,
            date = null,
            maxRecall = 1001,
            modifier = null,
            clearType = ArcaeaScoreClearType.FULL_RECALL,
            comment = null,
        )

        assertTrue(
            ArcaeaScoreValidatorMaxRecallOverflowWarning.conditionsMet(score, chartInfo)
        )
    }

    @Test
    fun pureMemoryFarLostNotZero() {
        val scoreHasFar = Score(
            id = 0,
            songId = "test",
            ratingClass = ArcaeaScoreRatingClass.BEYOND,
            score = 10002221,
            pure = 2221,
            far = 1,
            lost = 0,
            date = null,
            maxRecall = 2221,
            modifier = null,
            clearType = ArcaeaScoreClearType.PURE_MEMORY,
            comment = null,
        )
        val scoreHasLost = Score(
            id = 0,
            songId = "test",
            ratingClass = ArcaeaScoreRatingClass.BEYOND,
            score = 10002221,
            pure = 2221,
            far = 0,
            lost = 1,
            date = null,
            maxRecall = 2221,
            modifier = null,
            clearType = ArcaeaScoreClearType.PURE_MEMORY,
            comment = null,
        )
        val scoreHasNothing = Score(
            id = 0,
            songId = "test",
            ratingClass = ArcaeaScoreRatingClass.BEYOND,
            score = 10002221,
            pure = 2221,
            far = null,
            lost = null,
            date = null,
            maxRecall = 2221,
            modifier = null,
            clearType = ArcaeaScoreClearType.PURE_MEMORY,
            comment = null,
        )

        assertTrue(
            ArcaeaScoreValidatorPureMemoryFarLostNotZeroWarning.conditionsMet(scoreHasFar, null)
        )
        assertTrue(
            ArcaeaScoreValidatorPureMemoryFarLostNotZeroWarning.conditionsMet(scoreHasLost, null)
        )
        assertTrue(
            ArcaeaScoreValidatorPureMemoryFarLostNotZeroWarning.conditionsMet(scoreHasNothing, null)
        )
    }
}
