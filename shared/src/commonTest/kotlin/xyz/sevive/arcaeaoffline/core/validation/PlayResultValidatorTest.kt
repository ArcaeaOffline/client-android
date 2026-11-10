package xyz.sevive.arcaeaoffline.core.validation

import xyz.sevive.arcaeaoffline.core.calculators.calculateScoreRange
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PlayResultValidatorTest {
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

        assertNotNull(
            PlayResultWarning.MAX_RECALL_OVERFLOW.check(playResult, chartInfo),
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

        assertNotNull(
            PlayResultWarning.PURE_MEMORY_FAR_LOST_NOT_ZERO.check(
                playResultHasFar,
                null,
            ),
        )
        assertNotNull(
            PlayResultWarning.PURE_MEMORY_FAR_LOST_NOT_ZERO.check(
                playResultHasLost,
                null,
            ),
        )
        assertNotNull(
            PlayResultWarning.PURE_MEMORY_FAR_LOST_NOT_ZERO.check(
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

        assertNull(
            PlayResultWarning.FR_PM_MAX_RECALL_MISMATCH.check(
                playResultMismatched,
                null,
            ),
        )
        assertNull(
            PlayResultWarning.FR_PM_MAX_RECALL_MISMATCH.check(
                playResultNoMaxRecall,
                chartInfo,
            ),
        )
        assertNotNull(
            PlayResultWarning.FR_PM_MAX_RECALL_MISMATCH.check(
                playResultMismatched,
                chartInfo,
            ),
        )
        assertNull(
            PlayResultWarning.FR_PM_MAX_RECALL_MISMATCH.check(
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

        assertNotNull(
            PlayResultWarning.CLEAR_PFL_MISMATCH.check(playResult, chartInfo),
        )
        assertNull(
            PlayResultWarning.CLEAR_PFL_MISMATCH.check(trackLost, chartInfo),
        )
        assertNotNull(
            PlayResultWarning.CLEAR_PFL_MISMATCH.check(
                trackLost.copy(modifier = ArcaeaPlayResultModifier.NORMAL),
                chartInfo,
            ),
        )
        assertNull(
            PlayResultWarning.CLEAR_PFL_MISMATCH.check(
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

        assertNotNull(
            PlayResultWarning.SCORE_IMPOSSIBLE.check(playResultNegative, null),
        )
        assertNull(
            PlayResultWarning.SCORE_IMPOSSIBLE.check(playResultZero, null),
        )
        assertNull(
            PlayResultWarning.SCORE_IMPOSSIBLE.check(playResultTheoretical, chartInfo),
        )
        assertNotNull(
            PlayResultWarning.SCORE_IMPOSSIBLE.check(playResultBeyondTheoretical, chartInfo),
        )
        assertNull(
            PlayResultWarning.SCORE_IMPOSSIBLE.check(playResultBeyondTheoretical, chartInfoNotesNull),
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

        assertNotNull(
            PlayResultWarning.ALL_PURE_NOT_PM.check(playResult, null),
        )
        assertNotNull(
            PlayResultWarning.ALL_PURE_NOT_PM.check(
                playResult.copy(clearType = ArcaeaPlayResultClearType.TRACK_LOST),
                null,
            ),
        )
        assertNotNull(
            PlayResultWarning.ALL_PURE_NOT_PM.check(
                playResult.copy(clearType = ArcaeaPlayResultClearType.FULL_RECALL),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.ALL_PURE_NOT_PM.check(
                playResult.copy(clearType = ArcaeaPlayResultClearType.PURE_MEMORY),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.ALL_PURE_NOT_PM.check(
                playResult.copy(far = 1),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.ALL_PURE_NOT_PM.check(
                playResult.copy(far = null),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.ALL_PURE_NOT_PM.check(
                playResult.copy(lost = null),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.ALL_PURE_NOT_PM.check(
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

        assertNotNull(
            PlayResultWarning.PLAIN_CLEAR_WITHOUT_LOST.check(playResult, null),
        )
        assertNotNull(
            PlayResultWarning.PLAIN_CLEAR_WITHOUT_LOST.check(
                playResult.copy(clearType = ArcaeaPlayResultClearType.EASY_CLEAR),
                null,
            ),
        )
        assertNotNull(
            PlayResultWarning.PLAIN_CLEAR_WITHOUT_LOST.check(
                playResult.copy(clearType = ArcaeaPlayResultClearType.HARD_CLEAR),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.PLAIN_CLEAR_WITHOUT_LOST.check(
                playResult.copy(lost = 1),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.PLAIN_CLEAR_WITHOUT_LOST.check(
                playResult.copy(lost = null),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.PLAIN_CLEAR_WITHOUT_LOST.check(
                playResult.copy(clearType = ArcaeaPlayResultClearType.FULL_RECALL),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.PLAIN_CLEAR_WITHOUT_LOST.check(
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

        assertNotNull(
            PlayResultWarning.PM_SCORE_TOO_LOW.check(playResult, null),
        )
        assertNull(
            PlayResultWarning.PM_SCORE_TOO_LOW.check(
                playResult.copy(score = 10_000_000),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.PM_SCORE_TOO_LOW.check(
                playResult.copy(clearType = ArcaeaPlayResultClearType.FULL_RECALL),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.PM_SCORE_TOO_LOW.check(
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

        assertNotNull(
            PlayResultWarning.NEGATIVE_COUNT.check(
                playResult.copy(pure = -1),
                null,
            ),
        )
        assertNotNull(
            PlayResultWarning.NEGATIVE_COUNT.check(
                playResult.copy(far = -1),
                null,
            ),
        )
        assertNotNull(
            PlayResultWarning.NEGATIVE_COUNT.check(
                playResult.copy(lost = -1),
                null,
            ),
        )
        assertNotNull(
            PlayResultWarning.NEGATIVE_COUNT.check(
                playResult.copy(maxRecall = -1),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.NEGATIVE_COUNT.check(playResult, null),
        )
        assertNull(
            PlayResultWarning.NEGATIVE_COUNT.check(
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

        assertNotNull(
            PlayResultWarning.MAX_RECALL_OVERFLOW.check(playResult, chartInfo),
        )
        assertNull(
            PlayResultWarning.MAX_RECALL_OVERFLOW.check(
                playResult.copy(maxRecall = 915),
                chartInfo,
            ),
        )
        assertNotNull(
            PlayResultWarning.MAX_RECALL_OVERFLOW.check(
                playResult.copy(lost = null, maxRecall = 921),
                chartInfo,
            ),
        )
        assertNull(
            PlayResultWarning.MAX_RECALL_OVERFLOW.check(
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

        assertNotNull(
            PlayResultWarning.MAX_RECALL_OVERFLOW.check(playResult, chartInfo),
        )
        assertNull(
            PlayResultWarning.MAX_RECALL_OVERFLOW.check(
                playResult.copy(maxRecall = 820),
                chartInfo,
            ),
        )

        // An interrupted play: notes minus lost (915) is looser than the 820 judged notes.
        assertNotNull(
            PlayResultWarning.MAX_RECALL_OVERFLOW.check(
                playResult.copy(lost = 5),
                chartInfo,
            ),
        )

        // PURE + FAR exceed the note count, so notes minus lost (910) is the tighter bound.
        assertNotNull(
            PlayResultWarning.MAX_RECALL_OVERFLOW.check(
                playResult.copy(pure = 900, far = 30, lost = 10, maxRecall = 911),
                chartInfo,
            ),
        )
        assertNull(
            PlayResultWarning.MAX_RECALL_OVERFLOW.check(
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

        assertNotNull(
            PlayResultWarning.MODIFIER_CLEAR_TYPE_MISMATCH.check(playResult, null),
        )
        assertNotNull(
            PlayResultWarning.MODIFIER_CLEAR_TYPE_MISMATCH.check(
                playResult.copy(modifier = ArcaeaPlayResultModifier.HARD),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.MODIFIER_CLEAR_TYPE_MISMATCH.check(
                playResult.copy(modifier = ArcaeaPlayResultModifier.NORMAL),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.MODIFIER_CLEAR_TYPE_MISMATCH.check(
                playResult.copy(modifier = null),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.MODIFIER_CLEAR_TYPE_MISMATCH.check(
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

        assertNull(
            PlayResultWarning.SCORE_OUT_OF_RANGE.check(playResult, chartInfo),
        )
        assertNull(
            PlayResultWarning.SCORE_OUT_OF_RANGE.check(
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
            assertNull(
                PlayResultWarning.SCORE_OUT_OF_RANGE.check(playResult, corrupt),
                "notes=$notes",
            )
            assertNull(
                PlayResultWarning.SCORE_IMPOSSIBLE.check(playResult, corrupt),
                "notes=$notes",
            )
            assertNull(
                PlayResultWarning.PFL_OVERFLOW.check(playResult, corrupt),
                "notes=$notes",
            )
            assertNull(
                PlayResultWarning.MAX_RECALL_OVERFLOW.check(playResult, corrupt),
                "notes=$notes",
            )
            assertNull(
                PlayResultWarning.FR_PM_MAX_RECALL_MISMATCH.check(playResult, corrupt),
                "notes=$notes",
            )
            assertNull(
                PlayResultWarning.CLEAR_PFL_MISMATCH.check(playResult, corrupt),
                "notes=$notes",
            )
        }

        // A negative score stays impossible even when the note count is unusable.
        assertNotNull(
            PlayResultWarning.SCORE_IMPOSSIBLE.check(
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

        assertNotNull(
            PlayResultWarning.PFL_OVERFLOW.check(playResult, chartInfo),
        )
        assertNull(
            PlayResultWarning.PFL_OVERFLOW.check(
                playResult.copy(lost = 0),
                chartInfo,
            ),
        )
        assertNull(
            PlayResultWarning.PFL_OVERFLOW.check(
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
        assertNotNull(
            PlayResultWarning.SCORE_IMPOSSIBLE.check(playResult, chartInfo),
        )
        assertNull(
            PlayResultWarning.SCORE_OUT_OF_RANGE.check(playResult, chartInfo),
        )

        val negative = playResult.copy(score = -1)
        assertNotNull(
            PlayResultWarning.SCORE_IMPOSSIBLE.check(negative, chartInfo),
        )
        assertNull(
            PlayResultWarning.SCORE_OUT_OF_RANGE.check(negative, chartInfo),
        )

        // Below what 920 PURE judgments can score.
        val inconsistent = playResult.copy(score = 9_000_000)
        assertNull(
            PlayResultWarning.SCORE_IMPOSSIBLE.check(inconsistent, chartInfo),
        )
        assertNotNull(
            PlayResultWarning.SCORE_OUT_OF_RANGE.check(inconsistent, chartInfo),
        )

        // validate() reports the score problem once.
        assertEquals(
            listOf(PlayResultWarning.SCORE_IMPOSSIBLE),
            PlayResultValidator.validate(playResult, chartInfo).map { it.warning },
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
            PlayResultValidator
                .validate(playResult, chartInfo, ArcaeaScoringMode.B50)
                .any { it.warning == PlayResultWarning.CLEAR_TYPE_MISSING },
        )
        assertFalse(
            PlayResultValidator
                .validate(playResult, chartInfo, ArcaeaScoringMode.B30_R10)
                .any { it.warning == PlayResultWarning.CLEAR_TYPE_MISSING },
        )
        assertFalse(
            PlayResultValidator
                .validate(
                    playResult.copy(clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR),
                    chartInfo,
                    ArcaeaScoringMode.B50,
                ).any { it.warning == PlayResultWarning.CLEAR_TYPE_MISSING },
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

        assertNotNull(
            PlayResultWarning.CLEAR_TYPE_MISSING.check(playResult, null),
        )
        assertNull(
            PlayResultWarning.CLEAR_TYPE_MISSING.check(
                playResult.copy(clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR),
                null,
            ),
        )
    }

    @Test
    fun emptyPlayResult() {
        val playResult =
            PlayResult(
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = 0,
                pure = 0,
                far = 0,
                lost = 0,
            )

        assertNotNull(
            PlayResultWarning.EMPTY_PLAY_RESULT.check(playResult, null),
        )
        assertNotNull(
            PlayResultWarning.EMPTY_PLAY_RESULT.check(
                playResult.copy(pure = null, far = null, lost = null),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.EMPTY_PLAY_RESULT.check(
                playResult.copy(lost = 1),
                null,
            ),
        )
        assertNull(
            PlayResultWarning.EMPTY_PLAY_RESULT.check(
                playResult.copy(score = 1),
                null,
            ),
        )
    }

    @Test
    fun warningDetails() {
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
                score = 9_000_000,
                pure = 900,
                far = 20,
                lost = 0,
            )

        assertEquals(
            PlayResultWarningDetail.ScoreOutOfRange(
                actual = 9_000_000,
                expected = calculateScoreRange(920, 900, 20),
                pure = 900,
                far = 20,
            ),
            PlayResultWarning.SCORE_OUT_OF_RANGE.check(playResult, chartInfo),
        )

        assertEquals(
            PlayResultWarningDetail.ScoreTooLow(actual = 9_900_000, minimum = 10_000_000),
            PlayResultWarning.PM_SCORE_TOO_LOW.check(
                playResult.copy(
                    score = 9_900_000,
                    clearType = ArcaeaPlayResultClearType.PURE_MEMORY,
                ),
                chartInfo,
            ),
        )

        assertEquals(
            PlayResultWarningDetail.ScoreNegative(actual = -1),
            PlayResultWarning.SCORE_IMPOSSIBLE.check(playResult.copy(score = -1), chartInfo),
        )
        assertEquals(
            PlayResultWarningDetail.ScoreBeyondMaximum(actual = 10_000_921, maximum = 10_000_920),
            PlayResultWarning.SCORE_IMPOSSIBLE.check(playResult.copy(score = 10_000_921), chartInfo),
        )

        assertEquals(
            PlayResultWarningDetail.Judgments(pure = 917, far = 3, lost = 1, notes = 920),
            PlayResultWarning.PFL_OVERFLOW.check(
                playResult.copy(pure = 917, far = 3, lost = 1),
                chartInfo,
            ),
        )
        // The overflow warning is still raised when a count is unknown, but there is no complete evidence.
        assertEquals(
            PlayResultWarningDetail.None,
            PlayResultWarning.PFL_OVERFLOW.check(
                playResult.copy(pure = null, far = 3, lost = 1),
                chartInfo.copy(notes = 3),
            ),
        )

        assertEquals(
            PlayResultWarningDetail.MaxRecall(actual = 921, expected = 920),
            PlayResultWarning.MAX_RECALL_OVERFLOW.check(
                playResult.copy(pure = 917, far = 3, lost = 0, maxRecall = 921),
                chartInfo,
            ),
        )

        assertEquals(
            PlayResultWarningDetail.MaxRecall(actual = 919, expected = 920),
            PlayResultWarning.FR_PM_MAX_RECALL_MISMATCH.check(
                playResult.copy(
                    clearType = ArcaeaPlayResultClearType.FULL_RECALL,
                    maxRecall = 919,
                ),
                chartInfo,
            ),
        )

        assertEquals(
            PlayResultWarningDetail.Judgments(pure = 900, far = 20, lost = 1, notes = 920),
            PlayResultWarning.CLEAR_PFL_MISMATCH.check(
                playResult.copy(lost = 1, clearType = ArcaeaPlayResultClearType.NORMAL_CLEAR),
                chartInfo,
            ),
        )

        assertEquals(
            PlayResultWarningDetail.FarLost(far = 1, lost = 0),
            PlayResultWarning.PURE_MEMORY_FAR_LOST_NOT_ZERO.check(
                playResult.copy(
                    clearType = ArcaeaPlayResultClearType.PURE_MEMORY,
                    far = 1,
                    lost = 0,
                ),
                chartInfo,
            ),
        )

        assertEquals(
            PlayResultWarningDetail.Lost(actual = 1),
            PlayResultWarning.FULL_RECALL_LOST_NOT_ZERO.check(
                playResult.copy(clearType = ArcaeaPlayResultClearType.FULL_RECALL, lost = 1),
                chartInfo,
            ),
        )
        assertEquals(
            PlayResultWarningDetail.None,
            PlayResultWarning.FULL_RECALL_LOST_NOT_ZERO.check(
                playResult.copy(clearType = ArcaeaPlayResultClearType.FULL_RECALL, lost = null),
                chartInfo,
            ),
        )
    }
}
