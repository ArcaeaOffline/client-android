package xyz.sevive.arcaeaoffline.ui.helpers

import androidx.annotation.StringRes
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorAllPureNotPmWarning
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorClearPflMismatchWarning
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorClearTypeMissingWarning
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorFrPmMaxRecallMismatchWarning
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorFullRecallLostNotZeroWarning
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorMaxRecallOverflowWarning
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorModifierClearTypeMismatchWarning
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorNegativeCountWarning
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorPflOverflowWarning
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorPlainClearWithoutLostWarning
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorPmScoreTooLowWarning
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorPureMemoryFarLostNotZeroWarning
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorScoreImpossibleWarning
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorScoreIsZeroWarning
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorScoreOutOfRangeWarning
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidatorWarning

@StringRes
internal fun ArcaeaPlayResultValidatorWarning.titleRes(): Int =
    when (this) {
        ArcaeaPlayResultValidatorScoreIsZeroWarning -> R.string.play_result_validator_SCORE_IS_ZERO_title
        ArcaeaPlayResultValidatorScoreOutOfRangeWarning -> R.string.play_result_validator_SCORE_OUT_OF_RANGE_title
        ArcaeaPlayResultValidatorPmScoreTooLowWarning -> R.string.play_result_validator_PM_SCORE_TOO_LOW_title
        ArcaeaPlayResultValidatorScoreImpossibleWarning -> R.string.play_result_validator_SCORE_IMPOSSIBLE_title
        ArcaeaPlayResultValidatorPflOverflowWarning -> R.string.play_result_validator_PFL_OVERFLOW_title
        ArcaeaPlayResultValidatorMaxRecallOverflowWarning -> R.string.play_result_validator_MAX_RECALL_OVERFLOW_title
        ArcaeaPlayResultValidatorNegativeCountWarning -> R.string.play_result_validator_NEGATIVE_COUNT_title
        ArcaeaPlayResultValidatorFrPmMaxRecallMismatchWarning -> R.string.play_result_validator_FR_PM_MAX_RECALL_MISMATCH_title
        ArcaeaPlayResultValidatorClearPflMismatchWarning -> R.string.play_result_validator_CLEAR_PFL_MISMATCH_title
        ArcaeaPlayResultValidatorFullRecallLostNotZeroWarning -> R.string.play_result_validator_FULL_RECALL_LOST_NOT_ZERO_title
        ArcaeaPlayResultValidatorPlainClearWithoutLostWarning -> R.string.play_result_validator_PLAIN_CLEAR_WITHOUT_LOST_title
        ArcaeaPlayResultValidatorPureMemoryFarLostNotZeroWarning -> R.string.play_result_validator_PURE_MEMORY_FAR_LOST_NOT_ZERO_title
        ArcaeaPlayResultValidatorAllPureNotPmWarning -> R.string.play_result_validator_ALL_PURE_NOT_PM_title
        ArcaeaPlayResultValidatorModifierClearTypeMismatchWarning -> R.string.play_result_validator_MODIFIER_CLEAR_TYPE_MISMATCH_title
        ArcaeaPlayResultValidatorClearTypeMissingWarning -> R.string.play_result_validator_CLEAR_TYPE_MISSING_title
    }

@StringRes
internal fun ArcaeaPlayResultValidatorWarning.messageRes(): Int =
    when (this) {
        ArcaeaPlayResultValidatorScoreIsZeroWarning -> R.string.play_result_validator_SCORE_IS_ZERO_message
        ArcaeaPlayResultValidatorScoreOutOfRangeWarning -> R.string.play_result_validator_SCORE_OUT_OF_RANGE_message
        ArcaeaPlayResultValidatorPmScoreTooLowWarning -> R.string.play_result_validator_PM_SCORE_TOO_LOW_message
        ArcaeaPlayResultValidatorScoreImpossibleWarning -> R.string.play_result_validator_SCORE_IMPOSSIBLE_message
        ArcaeaPlayResultValidatorPflOverflowWarning -> R.string.play_result_validator_PFL_OVERFLOW_message
        ArcaeaPlayResultValidatorMaxRecallOverflowWarning -> R.string.play_result_validator_MAX_RECALL_OVERFLOW_message
        ArcaeaPlayResultValidatorNegativeCountWarning -> R.string.play_result_validator_NEGATIVE_COUNT_message
        ArcaeaPlayResultValidatorFrPmMaxRecallMismatchWarning -> R.string.play_result_validator_FR_PM_MAX_RECALL_MISMATCH_message
        ArcaeaPlayResultValidatorClearPflMismatchWarning -> R.string.play_result_validator_CLEAR_PFL_MISMATCH_message
        ArcaeaPlayResultValidatorFullRecallLostNotZeroWarning -> R.string.play_result_validator_FULL_RECALL_LOST_NOT_ZERO_message
        ArcaeaPlayResultValidatorPlainClearWithoutLostWarning -> R.string.play_result_validator_PLAIN_CLEAR_WITHOUT_LOST_message
        ArcaeaPlayResultValidatorPureMemoryFarLostNotZeroWarning -> R.string.play_result_validator_PURE_MEMORY_FAR_LOST_NOT_ZERO_message
        ArcaeaPlayResultValidatorAllPureNotPmWarning -> R.string.play_result_validator_ALL_PURE_NOT_PM_message
        ArcaeaPlayResultValidatorModifierClearTypeMismatchWarning -> R.string.play_result_validator_MODIFIER_CLEAR_TYPE_MISMATCH_message
        ArcaeaPlayResultValidatorClearTypeMissingWarning -> R.string.play_result_validator_CLEAR_TYPE_MISSING_message
    }
