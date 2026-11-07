package xyz.sevive.arcaeaoffline.ui.helpers

import androidx.annotation.StringRes
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.validation.PlayResultWarning

@StringRes
internal fun PlayResultWarning.titleRes(): Int =
    when (this) {
        PlayResultWarning.SCORE_IS_ZERO -> R.string.play_result_validator_SCORE_IS_ZERO_title
        PlayResultWarning.PURE_MEMORY_FAR_LOST_NOT_ZERO -> R.string.play_result_validator_PURE_MEMORY_FAR_LOST_NOT_ZERO_title
        PlayResultWarning.ALL_PURE_NOT_PM -> R.string.play_result_validator_ALL_PURE_NOT_PM_title
        PlayResultWarning.SCORE_OUT_OF_RANGE -> R.string.play_result_validator_SCORE_OUT_OF_RANGE_title
        PlayResultWarning.SCORE_IMPOSSIBLE -> R.string.play_result_validator_SCORE_IMPOSSIBLE_title
        PlayResultWarning.PM_SCORE_TOO_LOW -> R.string.play_result_validator_PM_SCORE_TOO_LOW_title
        PlayResultWarning.PFL_OVERFLOW -> R.string.play_result_validator_PFL_OVERFLOW_title
        PlayResultWarning.MAX_RECALL_OVERFLOW -> R.string.play_result_validator_MAX_RECALL_OVERFLOW_title
        PlayResultWarning.NEGATIVE_COUNT -> R.string.play_result_validator_NEGATIVE_COUNT_title
        PlayResultWarning.FR_PM_MAX_RECALL_MISMATCH -> R.string.play_result_validator_FR_PM_MAX_RECALL_MISMATCH_title
        PlayResultWarning.FULL_RECALL_LOST_NOT_ZERO -> R.string.play_result_validator_FULL_RECALL_LOST_NOT_ZERO_title
        PlayResultWarning.PLAIN_CLEAR_WITHOUT_LOST -> R.string.play_result_validator_PLAIN_CLEAR_WITHOUT_LOST_title
        PlayResultWarning.CLEAR_PFL_MISMATCH -> R.string.play_result_validator_CLEAR_PFL_MISMATCH_title
        PlayResultWarning.MODIFIER_CLEAR_TYPE_MISMATCH -> R.string.play_result_validator_MODIFIER_CLEAR_TYPE_MISMATCH_title
        PlayResultWarning.CLEAR_TYPE_MISSING -> R.string.play_result_validator_CLEAR_TYPE_MISSING_title
    }

@StringRes
internal fun PlayResultWarning.messageRes(): Int =
    when (this) {
        PlayResultWarning.SCORE_IS_ZERO -> R.string.play_result_validator_SCORE_IS_ZERO_message
        PlayResultWarning.PURE_MEMORY_FAR_LOST_NOT_ZERO -> R.string.play_result_validator_PURE_MEMORY_FAR_LOST_NOT_ZERO_message
        PlayResultWarning.ALL_PURE_NOT_PM -> R.string.play_result_validator_ALL_PURE_NOT_PM_message
        PlayResultWarning.SCORE_OUT_OF_RANGE -> R.string.play_result_validator_SCORE_OUT_OF_RANGE_message
        PlayResultWarning.SCORE_IMPOSSIBLE -> R.string.play_result_validator_SCORE_IMPOSSIBLE_message
        PlayResultWarning.PM_SCORE_TOO_LOW -> R.string.play_result_validator_PM_SCORE_TOO_LOW_message
        PlayResultWarning.PFL_OVERFLOW -> R.string.play_result_validator_PFL_OVERFLOW_message
        PlayResultWarning.MAX_RECALL_OVERFLOW -> R.string.play_result_validator_MAX_RECALL_OVERFLOW_message
        PlayResultWarning.NEGATIVE_COUNT -> R.string.play_result_validator_NEGATIVE_COUNT_message
        PlayResultWarning.FR_PM_MAX_RECALL_MISMATCH -> R.string.play_result_validator_FR_PM_MAX_RECALL_MISMATCH_message
        PlayResultWarning.FULL_RECALL_LOST_NOT_ZERO -> R.string.play_result_validator_FULL_RECALL_LOST_NOT_ZERO_message
        PlayResultWarning.PLAIN_CLEAR_WITHOUT_LOST -> R.string.play_result_validator_PLAIN_CLEAR_WITHOUT_LOST_message
        PlayResultWarning.CLEAR_PFL_MISMATCH -> R.string.play_result_validator_CLEAR_PFL_MISMATCH_message
        PlayResultWarning.MODIFIER_CLEAR_TYPE_MISMATCH -> R.string.play_result_validator_MODIFIER_CLEAR_TYPE_MISMATCH_message
        PlayResultWarning.CLEAR_TYPE_MISSING -> R.string.play_result_validator_CLEAR_TYPE_MISSING_message
    }
