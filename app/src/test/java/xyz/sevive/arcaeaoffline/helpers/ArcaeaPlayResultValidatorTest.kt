package xyz.sevive.arcaeaoffline.helpers

import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

class ArcaeaPlayResultValidatorTest {
    @Test
    fun maxRecallOverflow() {
        val chartInfo = ChartInfo(
            songId = "test",
            ratingClass = ArcaeaRatingClass.FUTURE,
            constant = 95,
            notes = 920,
        )

        val playResult = PlayResult(
            id = 0,
            songId = "test",
            ratingClass = ArcaeaRatingClass.FUTURE,
            score = 9984560,
            pure = 917,
            far = 3,
            lost = 0,
            date = null,
            maxRecall = 1001,
            modifier = null,
            clearType = ArcaeaPlayResultClearType.FULL_RECALL,
            comment = null,
        )

        assertTrue(
            ArcaeaPlayResultValidatorMaxRecallOverflowWarning.conditionsMet(playResult, chartInfo)
        )
    }

    @Test
    fun pureMemoryFarLostNotZero() {
        val playResultHasFar = PlayResult(
            id = 0,
            songId = "test",
            ratingClass = ArcaeaRatingClass.BEYOND,
            score = 10002221,
            pure = 2221,
            far = 1,
            lost = 0,
            date = null,
            maxRecall = 2221,
            modifier = null,
            clearType = ArcaeaPlayResultClearType.PURE_MEMORY,
            comment = null,
        )
        val playResultHasLost = PlayResult(
            id = 0,
            songId = "test",
            ratingClass = ArcaeaRatingClass.BEYOND,
            score = 10002221,
            pure = 2221,
            far = 0,
            lost = 1,
            date = null,
            maxRecall = 2221,
            modifier = null,
            clearType = ArcaeaPlayResultClearType.PURE_MEMORY,
            comment = null,
        )
        val playResultHasNothing = PlayResult(
            id = 0,
            songId = "test",
            ratingClass = ArcaeaRatingClass.BEYOND,
            score = 10002221,
            pure = 2221,
            far = null,
            lost = null,
            date = null,
            maxRecall = 2221,
            modifier = null,
            clearType = ArcaeaPlayResultClearType.PURE_MEMORY,
            comment = null,
        )

        assertTrue(
            ArcaeaPlayResultValidatorPureMemoryFarLostNotZeroWarning.conditionsMet(
                playResultHasFar,
                null
            )
        )
        assertTrue(
            ArcaeaPlayResultValidatorPureMemoryFarLostNotZeroWarning.conditionsMet(
                playResultHasLost,
                null
            )
        )
        assertTrue(
            ArcaeaPlayResultValidatorPureMemoryFarLostNotZeroWarning.conditionsMet(
                playResultHasNothing,
                null
            )
        )
    }
}
