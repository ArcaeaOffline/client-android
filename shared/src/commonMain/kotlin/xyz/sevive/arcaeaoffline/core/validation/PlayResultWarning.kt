package xyz.sevive.arcaeaoffline.core.validation

import xyz.sevive.arcaeaoffline.core.calculators.calculateScoreRange
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

/**
 * The chart's note count when it is usable for validation, or `null` when the count is missing
 * or non-positive.
 */
private fun ChartInfo?.usableNotes(): Int? = this?.notes?.takeIf { it > 0 }

/** The highest score a play can reach on a chart of [notes]: every PURE earns the shiny PURE bonus. */
private fun maxScore(notes: Int): Int = 10_000_000 + notes

/** Which scoring modes a warning takes part in. */
enum class PlayResultWarningScope {
    ALWAYS,
    B50_ONLY,
    ;

    /**
     * Whether this scope takes part in validation for [scoringMode]. A null mode runs the general
     * validation, which leaves out [PlayResultWarningScope.B50_ONLY].
     */
    fun appliesTo(scoringMode: ArcaeaScoringMode?): Boolean = this == ALWAYS || scoringMode == ArcaeaScoringMode.B50
}

/**
 * A data problem detected in a [PlayResult].
 *
 * Four null-value policies exist:
 *
 * * Contradiction warnings need every compared value. An unknown value (a null [PlayResult]
 *   field or a missing or non-positive [ChartInfo.notes]) makes the check pass silently.
 * * Overflow warnings ignore an unknown count. A known value could only tighten the check, so
 *   an overflow can be missed but never invented.
 * * Suspicion warnings verify the judgment counts implied by a claimed clear type. Unknown
 *   counts cannot confirm the claim, so the warning is still raised.
 * * EMPTY_PLAY_RESULT treats a null judgment count as 0, because the editor cannot distinguish
 *   an untouched count from one it cleared.
 */
enum class PlayResultWarning(
    val scope: PlayResultWarningScope = PlayResultWarningScope.ALWAYS,
) {
    EMPTY_PLAY_RESULT {
        override fun check(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ) = if (
            playResult.score == 0 &&
            (playResult.pure ?: 0) == 0 &&
            (playResult.far ?: 0) == 0 &&
            (playResult.lost ?: 0) == 0
        ) {
            PlayResultWarningDetail.None
        } else {
            null
        }
    },

    PURE_MEMORY_FAR_LOST_NOT_ZERO {
        override fun check(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ): PlayResultWarningDetail? {
            if (playResult.clearType != ArcaeaPlayResultClearType.PURE_MEMORY) return null

            val far = playResult.far
            val lost = playResult.lost
            if (far == 0 && lost == 0) return null

            return if (far != null && lost != null) {
                PlayResultWarningDetail.FarLost(far, lost)
            } else {
                PlayResultWarningDetail.None
            }
        }
    },

    ALL_PURE_NOT_PM {
        override fun check(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ) = if (playResult.clearType != null &&
            playResult.clearType != ArcaeaPlayResultClearType.PURE_MEMORY &&
            playResult.far == 0 &&
            playResult.lost == 0
        ) {
            PlayResultWarningDetail.None
        } else {
            null
        }
    },

    SCORE_OUT_OF_RANGE {
        override fun check(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ): PlayResultWarningDetail? {
            val notes = chartInfo.usableNotes() ?: return null

            // SCORE_IMPOSSIBLE reports scores outside [0, maxScore(notes)]. Without this guard the
            // range check below would report them too.
            if (playResult.score < 0 || playResult.score > maxScore(notes)) return null

            val pure = playResult.pure ?: return null
            val far = playResult.far ?: return null
            val expected = calculateScoreRange(notes, pure, far)

            return if (playResult.score in expected) {
                null
            } else {
                PlayResultWarningDetail.ScoreOutOfRange(playResult.score, expected, pure, far)
            }
        }
    },

    SCORE_IMPOSSIBLE {
        override fun check(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ): PlayResultWarningDetail? {
            if (playResult.score < 0) {
                return PlayResultWarningDetail.ScoreNegative(playResult.score)
            }

            val notes = chartInfo.usableNotes() ?: return null
            val maximum = maxScore(notes)
            return if (playResult.score > maximum) {
                PlayResultWarningDetail.ScoreBeyondMaximum(playResult.score, maximum)
            } else {
                null
            }
        }
    },

    PM_SCORE_TOO_LOW {
        override fun check(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ): PlayResultWarningDetail? =
            if (playResult.clearType == ArcaeaPlayResultClearType.PURE_MEMORY &&
                playResult.score < 10_000_000
            ) {
                PlayResultWarningDetail.ScoreTooLow(playResult.score, 10_000_000)
            } else {
                null
            }
    },

    PFL_OVERFLOW {
        override fun check(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ): PlayResultWarningDetail? {
            val notes = chartInfo.usableNotes() ?: return null

            val pure = playResult.pure
            val far = playResult.far
            val lost = playResult.lost
            val sum = (pure ?: 0) + (far ?: 0) + (lost ?: 0)
            if (sum <= notes) return null

            return if (pure != null && far != null && lost != null) {
                PlayResultWarningDetail.Judgments(pure, far, lost, notes)
            } else {
                PlayResultWarningDetail.None
            }
        }
    },

    MAX_RECALL_OVERFLOW {
        override fun check(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ): PlayResultWarningDetail? {
            val notes = chartInfo.usableNotes() ?: return null
            val maxRecall = playResult.maxRecall ?: return null

            val notesBound = notes - (playResult.lost ?: 0)
            val pure = playResult.pure
            val far = playResult.far

            // Max Recall cannot exceed the judged non-LOST count, nor the chart's notes minus LOST.
            // The smaller bound is the tighter check.
            val bound = if (pure != null && far != null) minOf(notesBound, pure + far) else notesBound
            return if (maxRecall > bound) {
                PlayResultWarningDetail.MaxRecall(maxRecall, bound)
            } else {
                null
            }
        }
    },

    NEGATIVE_COUNT {
        override fun check(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ) = if ((playResult.pure ?: 0) < 0 ||
            (playResult.far ?: 0) < 0 ||
            (playResult.lost ?: 0) < 0 ||
            (playResult.maxRecall ?: 0) < 0
        ) {
            PlayResultWarningDetail.None
        } else {
            null
        }
    },

    FR_PM_MAX_RECALL_MISMATCH {
        override fun check(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ): PlayResultWarningDetail? {
            if (playResult.clearType !in
                listOf(
                    ArcaeaPlayResultClearType.FULL_RECALL,
                    ArcaeaPlayResultClearType.PURE_MEMORY,
                )
            ) {
                return null
            }

            val notes = chartInfo.usableNotes() ?: return null
            val maxRecall = playResult.maxRecall ?: return null

            return if (maxRecall != notes) {
                PlayResultWarningDetail.MaxRecall(maxRecall, notes)
            } else {
                null
            }
        }
    },

    FULL_RECALL_LOST_NOT_ZERO {
        override fun check(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ): PlayResultWarningDetail? {
            if (playResult.clearType != ArcaeaPlayResultClearType.FULL_RECALL) return null

            val lost = playResult.lost
            if (lost == 0) return null

            return if (lost != null) {
                PlayResultWarningDetail.Lost(lost)
            } else {
                PlayResultWarningDetail.None
            }
        }
    },

    PLAIN_CLEAR_WITHOUT_LOST {
        override fun check(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ) = if (playResult.clearType in
            listOf(
                ArcaeaPlayResultClearType.NORMAL_CLEAR,
                ArcaeaPlayResultClearType.EASY_CLEAR,
                ArcaeaPlayResultClearType.HARD_CLEAR,
            ) &&
            playResult.lost == 0
        ) {
            PlayResultWarningDetail.None
        } else {
            null
        }
    },

    CLEAR_PFL_MISMATCH {
        override fun check(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ): PlayResultWarningDetail? {
            val notes = chartInfo.usableNotes() ?: return null
            val clearType = playResult.clearType ?: return null

            // A HARD play can be interrupted before the chart ends, so a Track Lost result may have
            // pure + far + lost below the note count. An unknown modifier cannot rule HARD out.
            // NORMAL and EASY gauges cannot interrupt a play.
            if (
                clearType == ArcaeaPlayResultClearType.TRACK_LOST &&
                playResult.modifier != ArcaeaPlayResultModifier.NORMAL &&
                playResult.modifier != ArcaeaPlayResultModifier.EASY
            ) {
                return null
            }

            val pure = playResult.pure ?: return null
            val far = playResult.far ?: return null
            val lost = playResult.lost ?: return null

            return if (pure + far + lost != notes) {
                PlayResultWarningDetail.Judgments(pure, far, lost, notes)
            } else {
                null
            }
        }
    },

    MODIFIER_CLEAR_TYPE_MISMATCH {
        override fun check(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ): PlayResultWarningDetail? {
            val modifier = playResult.modifier ?: return null
            val expectedModifier =
                when (playResult.clearType) {
                    ArcaeaPlayResultClearType.EASY_CLEAR -> ArcaeaPlayResultModifier.EASY
                    ArcaeaPlayResultClearType.HARD_CLEAR -> ArcaeaPlayResultModifier.HARD
                    ArcaeaPlayResultClearType.NORMAL_CLEAR -> ArcaeaPlayResultModifier.NORMAL
                    else -> return null
                }

            return if (modifier != expectedModifier) PlayResultWarningDetail.None else null
        }
    },

    CLEAR_TYPE_MISSING(scope = PlayResultWarningScope.B50_ONLY) {
        override fun check(
            playResult: PlayResult,
            chartInfo: ChartInfo?,
        ) = if (playResult.clearType == null) PlayResultWarningDetail.None else null
    },
    ;

    /**
     * The evidence this warning found, or `null` when it does not apply.
     * [PlayResultWarningDetail.None] means it applies but the compared values were incomplete.
     */
    abstract fun check(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): PlayResultWarningDetail?
}
