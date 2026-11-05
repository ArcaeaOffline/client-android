package xyz.sevive.arcaeaoffline.helpers

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

class ArcaeaPlayResultValidatorTest {
    @Test
    fun maxRecallOverflow() {
        val chartInfo =
            ChartInfo(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                constant = 95,
                notes = 920,
            )

        val playResult =
            PlayResult(
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
            ArcaeaPlayResultValidatorMaxRecallOverflowWarning.conditionsMet(playResult, chartInfo),
        )
    }

    @Test
    fun pureMemoryFarLostNotZero() {
        val playResultHasFar =
            PlayResult(
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
        val playResultHasLost =
            PlayResult(
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
        val playResultHasNothing =
            PlayResult(
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
                null,
            ),
        )
        assertTrue(
            ArcaeaPlayResultValidatorPureMemoryFarLostNotZeroWarning.conditionsMet(
                playResultHasLost,
                null,
            ),
        )
        assertTrue(
            ArcaeaPlayResultValidatorPureMemoryFarLostNotZeroWarning.conditionsMet(
                playResultHasNothing,
                null,
            ),
        )
    }

    @Test
    fun frPmMaxRecallMismatchSkipsUnknownValues() {
        val chartInfo =
            ChartInfo(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                constant = 95,
                notes = 920,
            )

        val playResultMatches =
            PlayResult(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = 10_000_000,
                pure = 920,
                far = 0,
                lost = 0,
                maxRecall = 920,
                clearType = ArcaeaPlayResultClearType.PURE_MEMORY,
            )
        val playResultMismatched = playResultMatches.copy(maxRecall = 900)
        val playResultNoMaxRecall = playResultMatches.copy(maxRecall = null)

        assertFalse(
            ArcaeaPlayResultValidatorFrPmMaxRecallMismatchWarning.conditionsMet(
                playResultMismatched,
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorFrPmMaxRecallMismatchWarning.conditionsMet(
                playResultNoMaxRecall,
                chartInfo,
            ),
        )
        assertTrue(
            ArcaeaPlayResultValidatorFrPmMaxRecallMismatchWarning.conditionsMet(
                playResultMismatched,
                chartInfo,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorFrPmMaxRecallMismatchWarning.conditionsMet(
                playResultMatches,
                chartInfo,
            ),
        )
    }

    @Test
    fun clearPflMismatchModifierNullHandling() {
        val chartInfo =
            ChartInfo(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                constant = 95,
                notes = 920,
            )

        val playResult =
            PlayResult(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = 9_800_000,
                pure = 880,
                far = 20,
                lost = 10,
                clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR,
            )
        val trackLost = playResult.copy(clearType = ArcaeaPlayResultClearType.TRACK_LOST)

        assertTrue(
            ArcaeaPlayResultValidatorClearPflMismatchWarning.conditionsMet(playResult, chartInfo),
        )
        assertFalse(
            ArcaeaPlayResultValidatorClearPflMismatchWarning.conditionsMet(trackLost, chartInfo),
        )
        assertTrue(
            ArcaeaPlayResultValidatorClearPflMismatchWarning.conditionsMet(
                trackLost.copy(modifier = ArcaeaPlayResultModifier.NORMAL),
                chartInfo,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorClearPflMismatchWarning.conditionsMet(
                trackLost.copy(modifier = ArcaeaPlayResultModifier.HARD),
                chartInfo,
            ),
        )
    }

    @Test
    fun scoreImpossible() {
        val chartInfo =
            ChartInfo(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                constant = 95,
                notes = 920,
            )
        val chartInfoNotesNull = chartInfo.copy(notes = null)

        val playResultNegative =
            PlayResult(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = -1,
            )
        val playResultZero = playResultNegative.copy(score = 0)
        val playResultTheoretical = playResultNegative.copy(score = 10_000_920)
        val playResultBeyondTheoretical = playResultNegative.copy(score = 10_000_921)

        assertTrue(
            ArcaeaPlayResultValidatorScoreImpossibleWarning.conditionsMet(playResultNegative, null),
        )
        assertFalse(
            ArcaeaPlayResultValidatorScoreImpossibleWarning.conditionsMet(playResultZero, null),
        )
        assertFalse(
            ArcaeaPlayResultValidatorScoreImpossibleWarning.conditionsMet(playResultTheoretical, chartInfo),
        )
        assertTrue(
            ArcaeaPlayResultValidatorScoreImpossibleWarning.conditionsMet(playResultBeyondTheoretical, chartInfo),
        )
        assertFalse(
            ArcaeaPlayResultValidatorScoreImpossibleWarning.conditionsMet(playResultBeyondTheoretical, chartInfoNotesNull),
        )
    }
}
