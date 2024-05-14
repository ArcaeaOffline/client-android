package xyz.sevive.arcaeaoffline.core.helpers

import android.content.Context
import androidx.core.content.res.ResourcesCompat
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.calculators.calculateArcaeaScoreRange
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreModifier
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.Score

sealed interface ArcaeaScoreValidatorWarning {
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
    fun conditionsMet(score: Score, chartInfo: ChartInfo? = null): Boolean
}

class ArcaeaScoreValidateScoreOutOfRangeWarning : ArcaeaScoreValidatorWarning {
    override val id = "SCORE_OUT_OF_RANGE"

    override val title = "Score out of range"
    override val titleId = R.string.score_validator_SCORE_OUT_OF_RANGE_title

    override val message = "Score out of range"
    override val messageId = R.string.score_validator_SCORE_OUT_OF_RANGE_message

    override fun conditionsMet(score: Score, chartInfo: ChartInfo?): Boolean {
        if (chartInfo?.notes == null || score.pure == null || score.far == null) {
            return false
        }

        val scoreRange = calculateArcaeaScoreRange(chartInfo.notes!!, score.pure!!, score.far!!)
        return scoreRange.contains(score.score)
    }
}

class ArcaeaScoreValidatorPflOverflowWarning : ArcaeaScoreValidatorWarning {
    override val id = "PFL_OVERFLOW"

    override val title = "Notes overflow"
    override val titleId = R.string.score_validator_PFL_OVERFLOW_title

    override val message = "pure + far + lost > chart note count"
    override val messageId = R.string.score_validator_PFL_OVERFLOW_message

    override fun conditionsMet(score: Score, chartInfo: ChartInfo?): Boolean {
        if (chartInfo?.notes == null) {
            return false
        }

        var sum = 0
        sum += score.pure ?: 0
        sum += score.far ?: 0
        sum += score.lost ?: 0

        return sum > chartInfo.notes!!
    }
}

class ArcaeaScoreValidatorMaxRecallOverflowWarning : ArcaeaScoreValidatorWarning {
    override val id = "MAX_RECALL_OVERFLOW"

    override val title = "Max recall overflow"
    override val titleId = R.string.score_validator_MAX_RECALL_OVERFLOW_title

    override val message = "max recall > chart note count"
    override val messageId = R.string.score_validator_MAX_RECALL_OVERFLOW_message

    override fun conditionsMet(score: Score, chartInfo: ChartInfo?): Boolean {
        if (chartInfo?.notes == null || score.maxRecall == null) {
            return false
        }

        return score.maxRecall!! > chartInfo.notes!!
    }
}

class ArcaeaScoreValidatorFrPmMaxRecallMismatchWarning : ArcaeaScoreValidatorWarning {
    override val id = "FR_PM_MAX_RECALL_MISMATCH"

    override val title = "FR/PM max recall mismatch"
    override val titleId = R.string.score_validator_FR_PM_MAX_RECALL_MISMATCH_title

    override val message = "A FR/PM play result should have same max recall with the chart notes."
    override val messageId = R.string.score_validator_FR_PM_MAX_RECALL_MISMATCH_message

    override fun conditionsMet(score: Score, chartInfo: ChartInfo?): Boolean {
        if (score.clearType != ArcaeaScoreClearType.FULL_RECALL.value || score.clearType != ArcaeaScoreClearType.PURE_MEMORY.value) {
            return false
        }

        return score.maxRecall != chartInfo?.notes
    }
}


class ArcaeaScoreValidatorClearPflMismatchWarning : ArcaeaScoreValidatorWarning {
    override val id = "CLEAR_PFL_MISMATCH"

    override val title = "Notes mismatch"
    override val titleId = R.string.score_validator_CLEAR_PFL_MISMATCH_title

    override val message = "pure + far + lost != chart note count"
    override val messageId = R.string.score_validator_CLEAR_PFL_MISMATCH_message

    override fun conditionsMet(score: Score, chartInfo: ChartInfo?): Boolean {
        if (score.clearType == null || score.modifier == null) {
            return false
        }

        // HARD LOST does not conform to this rule
        if (score.clearType == ArcaeaScoreClearType.TRACK_LOST.value && score.modifier == ArcaeaScoreModifier.HARD.value) {
            return false
        }

        if (score.pure == null || score.far == null || score.lost == null) {
            return false
        }

        return score.pure!! + score.far!! + score.lost!! != chartInfo?.notes
    }
}

class ArcaeaScoreValidatorFullRecallLostNotZeroWarning : ArcaeaScoreValidatorWarning {
    override val id = "FULL_RECALL_LOST_NOT_ZERO"

    override val title = "FULL RECALL?"
    override val titleId = R.string.score_validator_FULL_RECALL_LOST_NOT_ZERO_title

    override val message = "A FR play result should have 0 lost."
    override val messageId = R.string.score_validator_FULL_RECALL_LOST_NOT_ZERO_message

    override fun conditionsMet(score: Score, chartInfo: ChartInfo?): Boolean {
        if (score.clearType != ArcaeaScoreClearType.FULL_RECALL.value) {
            return false
        }

        return score.lost != 0
    }
}

class ArcaeaScoreValidatorPureMemoryFarLostNotZeroWarning : ArcaeaScoreValidatorWarning {
    override val id = "PURE_MEMORY_FAR_LOST_NOT_ZERO"

    override val title = "PURE MEMORY?"
    override val titleId = R.string.score_validator_PURE_MEMORY_FAR_LOST_NOT_ZERO_title

    override val message = "A PM play result should have 0 far and 0 lost."
    override val messageId = R.string.score_validator_PURE_MEMORY_FAR_LOST_NOT_ZERO_message

    override fun conditionsMet(score: Score, chartInfo: ChartInfo?): Boolean {
        if (score.clearType != ArcaeaScoreClearType.PURE_MEMORY.value) {
            return false
        }

        return score.far != 0 || score.lost != 0
    }
}

class ArcaeaScoreValidatorModifierClearTypeMismatchWarning : ArcaeaScoreValidatorWarning {
    override val id = "MODIFIER_CLEAR_TYPE_MISMATCH"

    override val title = "Modifier <> Clear type mismatch"
    override val titleId = R.string.score_validator_MODIFIER_CLEAR_TYPE_MISMATCH_title

    override val message = "easy clear != easy || hard clear != hard"
    override val messageId = R.string.score_validator_MODIFIER_CLEAR_TYPE_MISMATCH_message

    override fun conditionsMet(score: Score, chartInfo: ChartInfo?): Boolean {
        return (score.clearType == ArcaeaScoreClearType.EASY_CLEAR.value && score.modifier != ArcaeaScoreModifier.EASY.value) || (score.clearType == ArcaeaScoreClearType.HARD_CLEAR.value && score.modifier != ArcaeaScoreModifier.HARD.value)
    }
}

