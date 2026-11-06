package xyz.sevive.arcaeaoffline.helpers

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import xyz.sevive.arcaeaoffline.R
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
 * Two null-value policies exist:
 *
 * * Contradiction warnings need both compared values. An unknown value (a null [PlayResult]
 *   field or a missing or non-positive [ChartInfo.notes]) makes the check pass silently.
 * * Suspicion warnings, titled with a "?", verify the judgment counts implied by a claimed
 *   clear type. Unknown counts cannot confirm the claim and also trigger the warning.
 */
sealed interface ArcaeaPlayResultValidatorWarning {
    val id: String

    val title: String
    val titleId: Int

    fun getTitle(context: Context? = null): String {
        if (context == null || this.titleId == ResourcesCompat.ID_NULL) {
            return title
        }
        return context.getString(titleId)
    }

    val message: String?
    val messageId: Int

    fun getMessage(context: Context? = null): String? {
        if (context == null || this.messageId == ResourcesCompat.ID_NULL) {
            return message
        }
        return context.getString(messageId)
    }

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

    override val title = "Score is zero"
    override val titleId = R.string.play_result_validator_SCORE_IS_ZERO_title

    override val message = "Score is zero"
    override val messageId = R.string.play_result_validator_SCORE_IS_ZERO_message

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean = playResult.score == 0
}

data object ArcaeaPlayResultValidatorScoreOutOfRangeWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "SCORE_OUT_OF_RANGE"

    override val title = "Score out of range"
    override val titleId = R.string.play_result_validator_SCORE_OUT_OF_RANGE_title

    override val message = "Score out of range"
    override val messageId = R.string.play_result_validator_SCORE_OUT_OF_RANGE_message

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

    override val title = "PURE MEMORY score too low"
    override val titleId = R.string.play_result_validator_PM_SCORE_TOO_LOW_title

    override val message = "A PURE MEMORY play result scores at least 10000000."
    override val messageId = R.string.play_result_validator_PM_SCORE_TOO_LOW_message

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean =
        playResult.clearType == ArcaeaPlayResultClearType.PURE_MEMORY &&
            playResult.score < 10_000_000
}

data object ArcaeaPlayResultValidatorScoreImpossibleWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "SCORE_IMPOSSIBLE"

    override val title = "Impossible score"
    override val titleId = R.string.play_result_validator_SCORE_IMPOSSIBLE_title

    override val message = "The score is below zero, or above the Max PURE MEMORY value."
    override val messageId = R.string.play_result_validator_SCORE_IMPOSSIBLE_message

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

    override val title = "Notes overflow"
    override val titleId = R.string.play_result_validator_PFL_OVERFLOW_title

    override val message = "pure + far + lost > chart note count"
    override val messageId = R.string.play_result_validator_PFL_OVERFLOW_message

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

    override val title = "Max recall overflow"
    override val titleId = R.string.play_result_validator_MAX_RECALL_OVERFLOW_title

    override val message = "max recall > chart note count - lost"
    override val messageId = R.string.play_result_validator_MAX_RECALL_OVERFLOW_message

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean {
        val notes = chartInfo.usableNotes() ?: return false
        val maxRecall = playResult.maxRecall ?: return false

        return maxRecall > notes - (playResult.lost ?: 0)
    }
}

data object ArcaeaPlayResultValidatorNegativeCountWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "NEGATIVE_COUNT"

    override val title = "Negative count"
    override val titleId = R.string.play_result_validator_NEGATIVE_COUNT_title

    override val message = "pure, far, lost and max recall cannot be negative."
    override val messageId = R.string.play_result_validator_NEGATIVE_COUNT_message

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

    override val title = "FR/PM max recall mismatch"
    override val titleId = R.string.play_result_validator_FR_PM_MAX_RECALL_MISMATCH_title

    override val message = "A FR/PM play result should have same max recall with the chart notes."
    override val messageId = R.string.play_result_validator_FR_PM_MAX_RECALL_MISMATCH_message

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

    override val title = "Notes mismatch"
    override val titleId = R.string.play_result_validator_CLEAR_PFL_MISMATCH_title

    override val message = "pure + far + lost != chart note count (except HARD TRACK LOST)"
    override val messageId = R.string.play_result_validator_CLEAR_PFL_MISMATCH_message

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

    override val title = "FULL RECALL?"
    override val titleId = R.string.play_result_validator_FULL_RECALL_LOST_NOT_ZERO_title

    override val message = "A FR play result should have 0 lost."
    override val messageId = R.string.play_result_validator_FULL_RECALL_LOST_NOT_ZERO_message

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

    override val title = "Clear type & lost mismatch"
    override val titleId = R.string.play_result_validator_PLAIN_CLEAR_WITHOUT_LOST_title

    override val message = "A Normal/Easy/Hard Clear play result should have at least one lost."
    override val messageId = R.string.play_result_validator_PLAIN_CLEAR_WITHOUT_LOST_message

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

    override val title = "PURE MEMORY?"
    override val titleId = R.string.play_result_validator_PURE_MEMORY_FAR_LOST_NOT_ZERO_title

    override val message = "A PM play result should have 0 far and 0 lost."
    override val messageId = R.string.play_result_validator_PURE_MEMORY_FAR_LOST_NOT_ZERO_message

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

    override val title = "Should be PURE MEMORY"
    override val titleId = R.string.play_result_validator_ALL_PURE_NOT_PM_title

    override val message = "A play result with 0 far and 0 lost should be a PURE MEMORY."
    override val messageId = R.string.play_result_validator_ALL_PURE_NOT_PM_message

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

    override val title = "Modifier <> Clear type mismatch"
    override val titleId = R.string.play_result_validator_MODIFIER_CLEAR_TYPE_MISMATCH_title

    override val message = "easy clear != easy || hard clear != hard || normal clear != normal"
    override val messageId = R.string.play_result_validator_MODIFIER_CLEAR_TYPE_MISMATCH_message

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

    override val title = "Clear type missing"
    override val titleId = R.string.play_result_validator_CLEAR_TYPE_MISSING_title

    override val message =
        "The B50 rules count a play without a clear type as TRACK LOST, so it earns no clear bonus."
    override val messageId = R.string.play_result_validator_CLEAR_TYPE_MISSING_message

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean = playResult.clearType == null
}
