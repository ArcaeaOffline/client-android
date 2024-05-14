package xyz.sevive.arcaeaoffline.core.helpers

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.helpers.ArcaeaScoreValidator
import xyz.sevive.arcaeaoffline.helpers.ArcaeaScoreValidatorMaxRecallOverflowWarning
import xyz.sevive.arcaeaoffline.helpers.ArcaeaScoreValidatorPureMemoryFarLostNotZeroWarning

class ArcaeaScoreValidatorTest {
    private fun validateScoreResultIds(score: Score, chartInfo: ChartInfo?): List<String> {
        val validateResults = ArcaeaScoreValidator.validateScore(score, chartInfo)
        return validateResults.map { it.id }
    }

    @Test
    fun maxRecallOverflow() {
        val chartInfo = ChartInfo(songId = "test", ratingClass = 2, constant = 95, notes = 920)

        val scoreMaxRecallOverflow = Score(
            id = 0,
            songId = "test",
            ratingClass = 1,
            score = 9984560,
            pure = 917,
            far = 3,
            lost = 0,
            date = null,
            maxRecall = 1001,
            modifier = null,
            clearType = 2,
            comment = null,
        )

        val resultIds = validateScoreResultIds(scoreMaxRecallOverflow, chartInfo)
        assertTrue(
            resultIds.contains(ArcaeaScoreValidatorMaxRecallOverflowWarning().id)
        )
    }

    @Test
    fun pureMemoryFarLostNotZero() {
        val scoreHasFar = Score(
            id = 0,
            songId = "test",
            ratingClass = 3,
            score = 10002221,
            pure = 2221,
            far = 1,
            lost = 0,
            date = null,
            maxRecall = 2221,
            modifier = null,
            clearType = 3,
            comment = null,
        )
        val scoreHasLost = Score(
            id = 0,
            songId = "test",
            ratingClass = 3,
            score = 10002221,
            pure = 2221,
            far = 0,
            lost = 1,
            date = null,
            maxRecall = 2221,
            modifier = null,
            clearType = 3,
            comment = null,
        )
        val scoreHasNothing = Score(
            id = 0,
            songId = "test",
            ratingClass = 3,
            score = 10002221,
            pure = 2221,
            far = null,
            lost = null,
            date = null,
            maxRecall = 2221,
            modifier = null,
            clearType = 3,
            comment = null,
        )

        val hasFarResultIds = validateScoreResultIds(scoreHasFar, null)
        assertArrayEquals(
            arrayOf(ArcaeaScoreValidatorPureMemoryFarLostNotZeroWarning().id),
            hasFarResultIds.toTypedArray(),
        )

        val hasLostResultIds = validateScoreResultIds(scoreHasLost, null)
        assertArrayEquals(
            arrayOf(ArcaeaScoreValidatorPureMemoryFarLostNotZeroWarning().id),
            hasLostResultIds.toTypedArray(),
        )

        val hasNothingResultIds = validateScoreResultIds(scoreHasNothing, null)
        assertArrayEquals(
            arrayOf(ArcaeaScoreValidatorPureMemoryFarLostNotZeroWarning().id),
            hasNothingResultIds.toTypedArray(),
        )
    }
}
