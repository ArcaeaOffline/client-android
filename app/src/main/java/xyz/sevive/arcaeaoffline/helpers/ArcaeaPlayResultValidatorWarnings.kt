package xyz.sevive.arcaeaoffline.helpers

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.calculators.calculateArcaeaScoreRange
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

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
    fun conditionsMet(playResult: PlayResult, chartInfo: ChartInfo? = null): Boolean
}

data object ArcaeaScoreValidatePlayResultOutOfRangeWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "SCORE_OUT_OF_RANGE"

    override val title = "Score out of range"
    override val titleId = R.string.play_result_validator_SCORE_OUT_OF_RANGE_title

    override val message = "Score out of range"
    override val messageId = R.string.play_result_validator_SCORE_OUT_OF_RANGE_message

    override fun conditionsMet(playResult: PlayResult, chartInfo: ChartInfo?): Boolean {
        if (chartInfo?.notes == null || playResult.pure == null || playResult.far == null) {
            return false
        }

        val scoreRange =
            calculateArcaeaScoreRange(chartInfo.notes!!, playResult.pure!!, playResult.far!!)
        return !scoreRange.contains(playResult.score)
    }
}

data object ArcaeaPlayResultValidatorPflOverflowWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "PFL_OVERFLOW"

    override val title = "Notes overflow"
    override val titleId = R.string.play_result_validator_PFL_OVERFLOW_title

    override val message = "pure + far + lost > chart note count"
    override val messageId = R.string.play_result_validator_PFL_OVERFLOW_message

    override fun conditionsMet(playResult: PlayResult, chartInfo: ChartInfo?): Boolean {
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

    override fun conditionsMet(playResult: PlayResult, chartInfo: ChartInfo?): Boolean {
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

    override fun conditionsMet(playResult: PlayResult, chartInfo: ChartInfo?): Boolean {
        if (playResult.clearType !in listOf(
                ArcaeaPlayResultClearType.FULL_RECALL, ArcaeaPlayResultClearType.PURE_MEMORY
            )
        ) {
            return false
        }

        return playResult.maxRecall != chartInfo?.notes
    }
}


data object ArcaeaPlayResultValidatorClearPflMismatchWarning : ArcaeaPlayResultValidatorWarning {
    override val id = "CLEAR_PFL_MISMATCH"

    override val title = "Notes mismatch"
    override val titleId = R.string.play_result_validator_CLEAR_PFL_MISMATCH_title

    override val message = "pure + far + lost != chart note count"
    override val messageId = R.string.play_result_validator_CLEAR_PFL_MISMATCH_message

    override fun conditionsMet(playResult: PlayResult, chartInfo: ChartInfo?): Boolean {
        if (playResult.clearType == null || playResult.modifier == null) {
            return false
        }

        // HARD LOST does not conform to this rule
        if (playResult.clearType == ArcaeaPlayResultClearType.TRACK_LOST && playResult.modifier == ArcaeaPlayResultModifier.HARD) {
            return false
        }

        if (playResult.pure == null || playResult.far == null || playResult.lost == null) {
            return false
        }

        return playResult.pure!! + playResult.far!! + playResult.lost!! != chartInfo?.notes
    }
}

data object ArcaeaPlayResultValidatorFullRecallLostNotZeroWarning :
    ArcaeaPlayResultValidatorWarning {
    override val id = "FULL_RECALL_LOST_NOT_ZERO"

    override val title = "FULL RECALL?"
    override val titleId = R.string.play_result_validator_FULL_RECALL_LOST_NOT_ZERO_title

    override val message = "A FR play result should have 0 lost."
    override val messageId = R.string.play_result_validator_FULL_RECALL_LOST_NOT_ZERO_message

    override fun conditionsMet(playResult: PlayResult, chartInfo: ChartInfo?): Boolean {
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

    override fun conditionsMet(playResult: PlayResult, chartInfo: ChartInfo?): Boolean {
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

    override fun conditionsMet(playResult: PlayResult, chartInfo: ChartInfo?): Boolean {
        return (playResult.clearType == ArcaeaPlayResultClearType.EASY_CLEAR && playResult.modifier != ArcaeaPlayResultModifier.EASY) || (playResult.clearType == ArcaeaPlayResultClearType.HARD_CLEAR && playResult.modifier != ArcaeaPlayResultModifier.HARD)
    }
}

