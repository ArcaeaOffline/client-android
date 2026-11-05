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
 * A data problem detected in a [PlayResult].
 *
 * Two null-value policies exist:
 *
 * * Contradiction warnings need both compared values. An unknown value (a null [PlayResult]
 *   field or missing [ChartInfo.notes]) makes the check pass silently.
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
        if (chartInfo?.notes == null || playResult.pure == null || playResult.far == null) {
            return false
        }

        val scoreRange = calculateScoreRange(chartInfo.notes!!, playResult.pure!!, playResult.far!!)
        return !scoreRange.contains(playResult.score)
    }
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

        val notes = chartInfo?.notes ?: return false
        return playResult.score > 10_000_000 + notes
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
        if (chartInfo?.notes == null) {
            return false
        }

        var sum = 0
        sum += playResult.pure ?: 0
        sum += playResult.far ?: 0
        sum += playResult.lost ?: 0

        return sum > chartInfo.notes!!
    }
}

data object ArcaeaPlayResultValidatorMaxRecallOverflowWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "MAX_RECALL_OVERFLOW"

    override val title = "Max recall overflow"
    override val titleId = R.string.play_result_validator_MAX_RECALL_OVERFLOW_title

    override val message = "max recall > chart note count"
    override val messageId = R.string.play_result_validator_MAX_RECALL_OVERFLOW_message

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean {
        if (chartInfo?.notes == null || playResult.maxRecall == null) {
            return false
        }

        return playResult.maxRecall!! > chartInfo.notes!!
    }
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

        val notes = chartInfo?.notes ?: return false
        val maxRecall = playResult.maxRecall ?: return false

        return maxRecall != notes
    }
}

data object ArcaeaPlayResultValidatorClearPflMismatchWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "CLEAR_PFL_MISMATCH"

    override val title = "Notes mismatch"
    override val titleId = R.string.play_result_validator_CLEAR_PFL_MISMATCH_title

    override val message = "pure + far + lost != chart note count"
    override val messageId = R.string.play_result_validator_CLEAR_PFL_MISMATCH_message

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean {
        val notes = chartInfo?.notes ?: return false
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

data object ArcaeaPlayResultValidatorModifierClearTypeMismatchWarning :
    ArcaeaPlayResultValidatorWarning {
    override val id = "MODIFIER_CLEAR_TYPE_MISMATCH"

    override val title = "Modifier <> Clear type mismatch"
    override val titleId = R.string.play_result_validator_MODIFIER_CLEAR_TYPE_MISMATCH_title

    override val message = "easy clear != easy || hard clear != hard"
    override val messageId = R.string.play_result_validator_MODIFIER_CLEAR_TYPE_MISMATCH_message

    override fun conditionsMet(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
    ): Boolean =
        (playResult.clearType == ArcaeaPlayResultClearType.EASY_CLEAR && playResult.modifier != ArcaeaPlayResultModifier.EASY) ||
            (playResult.clearType == ArcaeaPlayResultClearType.HARD_CLEAR && playResult.modifier != ArcaeaPlayResultModifier.HARD)
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
