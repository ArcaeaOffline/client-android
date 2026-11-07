package xyz.sevive.arcaeaoffline.core.validation

import xyz.sevive.arcaeaoffline.core.calculators.calculateScoreRange
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

/**
 * The chart's note count when it is usable for validation, or `null` when the count is missing
 * or non-positive.
 */
private fun ChartInfo?.usableNotes(): Int? = this?.notes?.takeIf { it > 0 }

/** The highest score a play can reach on a chart of [notes]: every PURE earns the shiny PURE bonus. */
private fun maxScore(notes: Int): Int = 10_000_000 + notes

/**
 * A data problem detected in a [PlayResult].
 *
 * Three null-value policies exist:
 *
 * * Contradiction warnings need every compared value. An unknown value (a null [PlayResult]
 *   field or a missing or non-positive [ChartInfo.notes]) makes the check pass silently.
 * * Overflow warnings ignore an unknown count. A known value could only tighten the check, so
 *   an overflow can be missed but never invented.
 * * Suspicion warnings verify the judgment counts implied by a claimed clear type. Unknown
 *   counts cannot confirm the claim and also trigger the warning.
 */
sealed interface ArcaeaPlayResultValidatorWarning {
    val id: String

    /**
     * If the given values will trigger this warning, return `true`. That is, there are
     * problems with the given values.
     */
    fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo? = null,
    ): Boolean
}

data object ArcaeaPlayResultValidatorScoreIsZeroWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "SCORE_IS_ZERO"

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean = playResult.score == 0
}

data object ArcaeaPlayResultValidatorScoreOutOfRangeWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "SCORE_OUT_OF_RANGE"

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean {
        val notes = chartInfo.usableNotes() ?: return false

        // SCORE_IMPOSSIBLE reports scores outside [0, maxScore(notes)]. Without this guard the
        // range check below would report them too.
        if (playResult.score < 0 || playResult.score > maxScore(notes)) return false

        val pure = playResult.pure ?: return false
        val far = playResult.far ?: return false

        val scoreRange = calculateScoreRange(notes, pure, far)
        return !scoreRange.contains(playResult.score)
    }
}

data object ArcaeaPlayResultValidatorPmScoreTooLowWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "PM_SCORE_TOO_LOW"

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean =
        playResult.clearType == ArcaeaPlayResultClearType.PURE_MEMORY &&
            playResult.score < 10_000_000
}

data object ArcaeaPlayResultValidatorScoreImpossibleWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "SCORE_IMPOSSIBLE"

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean {
        if (playResult.score < 0) return true

        val notes = chartInfo.usableNotes() ?: return false
        return playResult.score > maxScore(notes)
    }
}

data object ArcaeaPlayResultValidatorPflOverflowWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "PFL_OVERFLOW"

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean {
        val notes = chartInfo.usableNotes() ?: return false

        var sum = 0
        sum += playResult.pure ?: 0
        sum += playResult.far ?: 0
        sum += playResult.lost ?: 0

        return sum > notes
    }
}

data object ArcaeaPlayResultValidatorMaxRecallOverflowWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "MAX_RECALL_OVERFLOW"

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean {
        val notes = chartInfo.usableNotes() ?: return false
        val maxRecall = playResult.maxRecall ?: return false

        val notesBound = notes - (playResult.lost ?: 0)
        val pure = playResult.pure
        val far = playResult.far

        // Max recall cannot exceed the judged non-LOST count, nor the chart's notes minus lost.
        // The smaller bound is the tighter check.
        val bound = if (pure != null && far != null) minOf(notesBound, pure + far) else notesBound
        return maxRecall > bound
    }
}

data object ArcaeaPlayResultValidatorNegativeCountWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "NEGATIVE_COUNT"

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean =
        (playResult.pure ?: 0) < 0 ||
            (playResult.far ?: 0) < 0 ||
            (playResult.lost ?: 0) < 0 ||
            (playResult.maxRecall ?: 0) < 0
}

data object ArcaeaPlayResultValidatorFrPmMaxRecallMismatchWarning :
    ArcaeaPlayResultValidatorWarning {
    override val id = "FR_PM_MAX_RECALL_MISMATCH"

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean {
        if (playResult.clearType !in
            listOf(
                ArcaeaPlayResultClearType.FULL_RECALL,
                ArcaeaPlayResultClearType.PURE_MEMORY,
            )
        ) {
            return false
        }

        val notes = chartInfo.usableNotes() ?: return false
        val maxRecall = playResult.maxRecall ?: return false

        return maxRecall != notes
    }
}

data object ArcaeaPlayResultValidatorClearPflMismatchWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "CLEAR_PFL_MISMATCH"

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean {
        val notes = chartInfo.usableNotes() ?: return false
        val clearType = playResult.clearType ?: return false

        // A HARD play can be interrupted before the chart ends, so a TRACK LOST result may have
        // pure + far + lost below the note count. An unknown modifier cannot rule HARD out.
        // NORMAL and EASY gauges cannot interrupt a play.
        if (
            clearType == ArcaeaPlayResultClearType.TRACK_LOST &&
            playResult.modifier != ArcaeaPlayResultModifier.NORMAL &&
            playResult.modifier != ArcaeaPlayResultModifier.EASY
        ) {
            return false
        }

        val pure = playResult.pure ?: return false
        val far = playResult.far ?: return false
        val lost = playResult.lost ?: return false

        return pure + far + lost != notes
    }
}

data object ArcaeaPlayResultValidatorFullRecallLostNotZeroWarning :
    ArcaeaPlayResultValidatorWarning {
    override val id = "FULL_RECALL_LOST_NOT_ZERO"

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean {
        if (playResult.clearType != ArcaeaPlayResultClearType.FULL_RECALL) {
            return false
        }

        return playResult.lost != 0
    }
}

data object ArcaeaPlayResultValidatorPlainClearWithoutLostWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "PLAIN_CLEAR_WITHOUT_LOST"

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean =
        playResult.clearType in
            listOf(
                ArcaeaPlayResultClearType.NORMAL_CLEAR,
                ArcaeaPlayResultClearType.EASY_CLEAR,
                ArcaeaPlayResultClearType.HARD_CLEAR,
            ) &&
            playResult.lost == 0
}

data object ArcaeaPlayResultValidatorPureMemoryFarLostNotZeroWarning :
    ArcaeaPlayResultValidatorWarning {
    override val id = "PURE_MEMORY_FAR_LOST_NOT_ZERO"

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean {
        if (playResult.clearType != ArcaeaPlayResultClearType.PURE_MEMORY) {
            return false
        }

        return playResult.far != 0 || playResult.lost != 0
    }
}

data object ArcaeaPlayResultValidatorAllPureNotPmWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "ALL_PURE_NOT_PM"

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean =
        playResult.clearType != null &&
            playResult.clearType != ArcaeaPlayResultClearType.PURE_MEMORY &&
            playResult.far == 0 &&
            playResult.lost == 0
}

data object ArcaeaPlayResultValidatorModifierClearTypeMismatchWarning :
    ArcaeaPlayResultValidatorWarning {
    override val id = "MODIFIER_CLEAR_TYPE_MISMATCH"

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean {
        val modifier = playResult.modifier ?: return false

        return (playResult.clearType == ArcaeaPlayResultClearType.EASY_CLEAR && modifier != ArcaeaPlayResultModifier.EASY) ||
            (playResult.clearType == ArcaeaPlayResultClearType.HARD_CLEAR && modifier != ArcaeaPlayResultModifier.HARD) ||
            (playResult.clearType == ArcaeaPlayResultClearType.NORMAL_CLEAR && modifier != ArcaeaPlayResultModifier.NORMAL)
    }
}

data object ArcaeaPlayResultValidatorClearTypeMissingWarning :
    ArcaeaPlayResultValidatorWarning {
    override val id = "CLEAR_TYPE_MISSING"

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean = playResult.clearType == null
}
