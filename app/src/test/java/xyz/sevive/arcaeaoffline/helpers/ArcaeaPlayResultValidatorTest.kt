package xyz.sevive.arcaeaoffline.helpers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
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

    @Test
    fun allPureNotPm() {
        val playResult =
            PlayResult(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = 10_000_000,
                pure = 920,
                far = 0,
                lost = 0,
                clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR,
            )

        assertTrue(
            ArcaeaPlayResultValidatorAllPureNotPmWarning.conditionsMet(playResult, null),
        )
        assertTrue(
            ArcaeaPlayResultValidatorAllPureNotPmWarning.conditionsMet(
                playResult.copy(clearType = ArcaeaPlayResultClearType.TRACK_LOST),
                null,
            ),
        )
        assertTrue(
            ArcaeaPlayResultValidatorAllPureNotPmWarning.conditionsMet(
                playResult.copy(clearType = ArcaeaPlayResultClearType.FULL_RECALL),
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorAllPureNotPmWarning.conditionsMet(
                playResult.copy(clearType = ArcaeaPlayResultClearType.PURE_MEMORY),
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorAllPureNotPmWarning.conditionsMet(
                playResult.copy(far = 1),
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorAllPureNotPmWarning.conditionsMet(
                playResult.copy(far = null),
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorAllPureNotPmWarning.conditionsMet(
                playResult.copy(lost = null),
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorAllPureNotPmWarning.conditionsMet(
                playResult.copy(clearType = null),
                null,
            ),
        )
    }

    @Test
    fun clearWithoutLost() {
        val playResult =
            PlayResult(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = 9_900_000,
                pure = 900,
                far = 20,
                lost = 0,
                clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR,
            )

        assertTrue(
            ArcaeaPlayResultValidatorPlainClearWithoutLostWarning.conditionsMet(playResult, null),
        )
        assertTrue(
            ArcaeaPlayResultValidatorPlainClearWithoutLostWarning.conditionsMet(
                playResult.copy(clearType = ArcaeaPlayResultClearType.EASY_CLEAR),
                null,
            ),
        )
        assertTrue(
            ArcaeaPlayResultValidatorPlainClearWithoutLostWarning.conditionsMet(
                playResult.copy(clearType = ArcaeaPlayResultClearType.HARD_CLEAR),
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorPlainClearWithoutLostWarning.conditionsMet(
                playResult.copy(lost = 1),
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorPlainClearWithoutLostWarning.conditionsMet(
                playResult.copy(lost = null),
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorPlainClearWithoutLostWarning.conditionsMet(
                playResult.copy(clearType = ArcaeaPlayResultClearType.FULL_RECALL),
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorPlainClearWithoutLostWarning.conditionsMet(
                playResult.copy(clearType = ArcaeaPlayResultClearType.TRACK_LOST),
                null,
            ),
        )
    }

    @Test
    fun pmScoreTooLow() {
        val playResult =
            PlayResult(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = 9_999_999,
                clearType = ArcaeaPlayResultClearType.PURE_MEMORY,
            )

        assertTrue(
            ArcaeaPlayResultValidatorPmScoreTooLowWarning.conditionsMet(playResult, null),
        )
        assertFalse(
            ArcaeaPlayResultValidatorPmScoreTooLowWarning.conditionsMet(
                playResult.copy(score = 10_000_000),
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorPmScoreTooLowWarning.conditionsMet(
                playResult.copy(clearType = ArcaeaPlayResultClearType.FULL_RECALL),
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorPmScoreTooLowWarning.conditionsMet(
                playResult.copy(clearType = null),
                null,
            ),
        )
    }

    @Test
    fun negativeCount() {
        val playResult =
            PlayResult(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = 9_900_000,
                pure = 900,
                far = 20,
                lost = 0,
                maxRecall = 920,
            )

        assertTrue(
            ArcaeaPlayResultValidatorNegativeCountWarning.conditionsMet(
                playResult.copy(pure = -1),
                null,
            ),
        )
        assertTrue(
            ArcaeaPlayResultValidatorNegativeCountWarning.conditionsMet(
                playResult.copy(far = -1),
                null,
            ),
        )
        assertTrue(
            ArcaeaPlayResultValidatorNegativeCountWarning.conditionsMet(
                playResult.copy(lost = -1),
                null,
            ),
        )
        assertTrue(
            ArcaeaPlayResultValidatorNegativeCountWarning.conditionsMet(
                playResult.copy(maxRecall = -1),
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorNegativeCountWarning.conditionsMet(playResult, null),
        )
        assertFalse(
            ArcaeaPlayResultValidatorNegativeCountWarning.conditionsMet(
                PlayResult(
                    songId = "test",
                    ratingClass = ArcaeaRatingClass.FUTURE,
                    score = 9_900_000,
                ),
                null,
            ),
        )
    }

    @Test
    fun maxRecallOverflowWithLost() {
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
                score = 9_900_000,
                lost = 5,
                maxRecall = 916,
            )

        assertTrue(
            ArcaeaPlayResultValidatorMaxRecallOverflowWarning.conditionsMet(playResult, chartInfo),
        )
        assertFalse(
            ArcaeaPlayResultValidatorMaxRecallOverflowWarning.conditionsMet(
                playResult.copy(maxRecall = 915),
                chartInfo,
            ),
        )
        assertTrue(
            ArcaeaPlayResultValidatorMaxRecallOverflowWarning.conditionsMet(
                playResult.copy(lost = null, maxRecall = 921),
                chartInfo,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorMaxRecallOverflowWarning.conditionsMet(
                playResult.copy(lost = null, maxRecall = 920),
                chartInfo,
            ),
        )
    }

    @Test
    fun maxRecallOverflowAgainstJudgedCounts() {
        val chartInfo =
            ChartInfo(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                constant = 95,
                notes = 920,
            )
        // 920 notes with 800 PURE and 20 FAR: only 820 judgments are not LOST.
        val playResult =
            PlayResult(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = 9_900_000,
                pure = 800,
                far = 20,
                lost = 0,
                maxRecall = 821,
            )

        assertTrue(
            ArcaeaPlayResultValidatorMaxRecallOverflowWarning.conditionsMet(playResult, chartInfo),
        )
        assertFalse(
            ArcaeaPlayResultValidatorMaxRecallOverflowWarning.conditionsMet(
                playResult.copy(maxRecall = 820),
                chartInfo,
            ),
        )

        // An interrupted play: notes minus lost (915) is looser than the 820 judged notes.
        assertTrue(
            ArcaeaPlayResultValidatorMaxRecallOverflowWarning.conditionsMet(
                playResult.copy(lost = 5),
                chartInfo,
            ),
        )

        // PURE + FAR exceed the note count, so notes minus lost (910) is the tighter bound.
        assertTrue(
            ArcaeaPlayResultValidatorMaxRecallOverflowWarning.conditionsMet(
                playResult.copy(pure = 900, far = 30, lost = 10, maxRecall = 911),
                chartInfo,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorMaxRecallOverflowWarning.conditionsMet(
                playResult.copy(pure = 900, far = 30, lost = 10, maxRecall = 910),
                chartInfo,
            ),
        )
    }

    @Test
    fun modifierClearTypeMismatchNormalDirection() {
        val playResult =
            PlayResult(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = 9_900_000,
                clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR,
                modifier = ArcaeaPlayResultModifier.EASY,
            )

        assertTrue(
            ArcaeaPlayResultValidatorModifierClearTypeMismatchWarning.conditionsMet(playResult, null),
        )
        assertTrue(
            ArcaeaPlayResultValidatorModifierClearTypeMismatchWarning.conditionsMet(
                playResult.copy(modifier = ArcaeaPlayResultModifier.HARD),
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorModifierClearTypeMismatchWarning.conditionsMet(
                playResult.copy(modifier = ArcaeaPlayResultModifier.NORMAL),
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorModifierClearTypeMismatchWarning.conditionsMet(
                playResult.copy(modifier = null),
                null,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorModifierClearTypeMismatchWarning.conditionsMet(
                playResult.copy(
                    clearType = ArcaeaPlayResultClearType.EASY_CLEAR,
                    modifier = ArcaeaPlayResultModifier.EASY,
                ),
                null,
            ),
        )
    }

    @Test
    fun scoreOutOfRangeNonPositiveNotes() {
        val chartInfo =
            ChartInfo(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                constant = 95,
                notes = 0,
            )
        val playResult =
            PlayResult(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = 9_900_000,
                pure = 900,
                far = 20,
            )

        assertFalse(
            ArcaeaPlayResultValidatorScoreOutOfRangeWarning.conditionsMet(playResult, chartInfo),
        )
        assertFalse(
            ArcaeaPlayResultValidatorScoreOutOfRangeWarning.conditionsMet(
                playResult,
                chartInfo.copy(notes = -5),
            ),
        )
    }

    @Test
    fun noteBasedWarningsSkipNonPositiveNotes() {
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
                score = 10_000_500,
                pure = 900,
                far = 20,
                lost = 0,
                maxRecall = 920,
                clearType = ArcaeaPlayResultClearType.FULL_RECALL,
            )

        for (notes in listOf(0, -5)) {
            val corrupt = chartInfo.copy(notes = notes)
            assertFalse(
                "notes=$notes",
                ArcaeaPlayResultValidatorScoreOutOfRangeWarning.conditionsMet(playResult, corrupt),
            )
            assertFalse(
                "notes=$notes",
                ArcaeaPlayResultValidatorScoreImpossibleWarning.conditionsMet(playResult, corrupt),
            )
            assertFalse(
                "notes=$notes",
                ArcaeaPlayResultValidatorPflOverflowWarning.conditionsMet(playResult, corrupt),
            )
            assertFalse(
                "notes=$notes",
                ArcaeaPlayResultValidatorMaxRecallOverflowWarning.conditionsMet(playResult, corrupt),
            )
            assertFalse(
                "notes=$notes",
                ArcaeaPlayResultValidatorFrPmMaxRecallMismatchWarning.conditionsMet(playResult, corrupt),
            )
            assertFalse(
                "notes=$notes",
                ArcaeaPlayResultValidatorClearPflMismatchWarning.conditionsMet(playResult, corrupt),
            )
        }

        // A negative score stays impossible even when the note count is unusable.
        assertTrue(
            ArcaeaPlayResultValidatorScoreImpossibleWarning.conditionsMet(
                playResult.copy(score = -1),
                chartInfo.copy(notes = 0),
            ),
        )
    }

    @Test
    fun pflOverflow() {
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
                score = 9_900_000,
                pure = 917,
                far = 3,
                lost = 1,
            )

        assertTrue(
            ArcaeaPlayResultValidatorPflOverflowWarning.conditionsMet(playResult, chartInfo),
        )
        assertFalse(
            ArcaeaPlayResultValidatorPflOverflowWarning.conditionsMet(
                playResult.copy(lost = 0),
                chartInfo,
            ),
        )
        assertFalse(
            ArcaeaPlayResultValidatorPflOverflowWarning.conditionsMet(
                playResult,
                chartInfo.copy(notes = 0),
            ),
        )
    }

    @Test
    fun scoreWarningsDoNotOverlap() {
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
                score = 10_000_921,
                pure = 920,
                far = 0,
                lost = 0,
            )

        // One point above the theoretical value.
        assertTrue(
            ArcaeaPlayResultValidatorScoreImpossibleWarning.conditionsMet(playResult, chartInfo),
        )
        assertFalse(
            ArcaeaPlayResultValidatorScoreOutOfRangeWarning.conditionsMet(playResult, chartInfo),
        )

        val negative = playResult.copy(score = -1)
        assertTrue(
            ArcaeaPlayResultValidatorScoreImpossibleWarning.conditionsMet(negative, chartInfo),
        )
        assertFalse(
            ArcaeaPlayResultValidatorScoreOutOfRangeWarning.conditionsMet(negative, chartInfo),
        )

        // Below what 920 PURE judgments can score.
        val inconsistent = playResult.copy(score = 9_000_000)
        assertFalse(
            ArcaeaPlayResultValidatorScoreImpossibleWarning.conditionsMet(inconsistent, chartInfo),
        )
        assertTrue(
            ArcaeaPlayResultValidatorScoreOutOfRangeWarning.conditionsMet(inconsistent, chartInfo),
        )

        // validate() reports the score problem once.
        assertEquals(
            listOf<ArcaeaPlayResultValidatorWarning>(ArcaeaPlayResultValidatorScoreImpossibleWarning),
            ArcaeaPlayResultValidator.validate(playResult, chartInfo),
        )
    }

    @Test
    fun warningRegistryIsComplete() {
        val allWarnings =
            setOf(
                ArcaeaPlayResultValidatorScoreIsZeroWarning,
                ArcaeaPlayResultValidatorPureMemoryFarLostNotZeroWarning,
                ArcaeaPlayResultValidatorAllPureNotPmWarning,
                ArcaeaPlayResultValidatorScoreOutOfRangeWarning,
                ArcaeaPlayResultValidatorScoreImpossibleWarning,
                ArcaeaPlayResultValidatorPmScoreTooLowWarning,
                ArcaeaPlayResultValidatorPflOverflowWarning,
                ArcaeaPlayResultValidatorMaxRecallOverflowWarning,
                ArcaeaPlayResultValidatorNegativeCountWarning,
                ArcaeaPlayResultValidatorFrPmMaxRecallMismatchWarning,
                ArcaeaPlayResultValidatorFullRecallLostNotZeroWarning,
                ArcaeaPlayResultValidatorPlainClearWithoutLostWarning,
                ArcaeaPlayResultValidatorClearPflMismatchWarning,
                ArcaeaPlayResultValidatorModifierClearTypeMismatchWarning,
                ArcaeaPlayResultValidatorClearTypeMissingWarning,
            )

        // CLEAR_TYPE_MISSING is only added by the B50 overload, so the general registry leaves it
        // out.
        assertEquals(
            allWarnings - ArcaeaPlayResultValidatorClearTypeMissingWarning,
            ArcaeaPlayResultValidator.WARNINGS.toSet(),
        )
    }

    @Test
    fun clearTypeMissingOnlyUnderB50() {
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
                score = 9_900_000,
                pure = 900,
                far = 20,
                lost = 0,
                maxRecall = 920,
                clearType = null,
            )

        assertTrue(
            ArcaeaPlayResultValidator
                .validate(playResult, chartInfo, ArcaeaScoringMode.B50)
                .contains(ArcaeaPlayResultValidatorClearTypeMissingWarning),
        )
        assertFalse(
            ArcaeaPlayResultValidator
                .validate(playResult, chartInfo, ArcaeaScoringMode.B30_R10)
                .contains(ArcaeaPlayResultValidatorClearTypeMissingWarning),
        )
        assertFalse(
            ArcaeaPlayResultValidator
                .validate(
                    playResult.copy(clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR),
                    chartInfo,
                    ArcaeaScoringMode.B50,
                ).contains(ArcaeaPlayResultValidatorClearTypeMissingWarning),
        )
    }

    @Test
    fun clearTypeMissing() {
        val playResult =
            PlayResult(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = 9_900_000,
                clearType = null,
            )

        assertTrue(
            ArcaeaPlayResultValidatorClearTypeMissingWarning.conditionsMet(playResult, null),
        )
        assertFalse(
            ArcaeaPlayResultValidatorClearTypeMissingWarning.conditionsMet(
                playResult.copy(clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR),
                null,
            ),
        )
    }

    @Test
    fun scoreIsZero() {
        val playResult =
            PlayResult(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = 0,
            )

        assertTrue(
            ArcaeaPlayResultValidatorScoreIsZeroWarning.conditionsMet(playResult, null),
        )
        assertFalse(
            ArcaeaPlayResultValidatorScoreIsZeroWarning.conditionsMet(
                playResult.copy(score = 1),
                null,
            ),
        )
    }
}
