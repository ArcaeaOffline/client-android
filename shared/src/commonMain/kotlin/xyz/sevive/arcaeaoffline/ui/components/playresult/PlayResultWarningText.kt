package xyz.sevive.arcaeaoffline.ui.components.playresult

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.pluralStringResource
import xyz.sevive.arcaeaoffline.core.validation.PlayResultWarning
import xyz.sevive.arcaeaoffline.resources.Res
import xyz.sevive.arcaeaoffline.resources.play_result_validator_all_pure_not_pm_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_all_pure_not_pm_title
import xyz.sevive.arcaeaoffline.resources.play_result_validator_clear_pfl_mismatch_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_clear_pfl_mismatch_title
import xyz.sevive.arcaeaoffline.resources.play_result_validator_clear_type_missing_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_clear_type_missing_title
import xyz.sevive.arcaeaoffline.resources.play_result_validator_fr_pm_max_recall_mismatch_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_fr_pm_max_recall_mismatch_title
import xyz.sevive.arcaeaoffline.resources.play_result_validator_full_recall_lost_not_zero_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_full_recall_lost_not_zero_title
import xyz.sevive.arcaeaoffline.resources.play_result_validator_max_recall_overflow_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_max_recall_overflow_title
import xyz.sevive.arcaeaoffline.resources.play_result_validator_modifier_clear_type_mismatch_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_modifier_clear_type_mismatch_title
import xyz.sevive.arcaeaoffline.resources.play_result_validator_negative_count_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_negative_count_title
import xyz.sevive.arcaeaoffline.resources.play_result_validator_pfl_overflow_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_pfl_overflow_title
import xyz.sevive.arcaeaoffline.resources.play_result_validator_plain_clear_without_lost_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_plain_clear_without_lost_title
import xyz.sevive.arcaeaoffline.resources.play_result_validator_pm_score_too_low_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_pm_score_too_low_title
import xyz.sevive.arcaeaoffline.resources.play_result_validator_pure_memory_far_lost_not_zero_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_pure_memory_far_lost_not_zero_title
import xyz.sevive.arcaeaoffline.resources.play_result_validator_score_impossible_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_score_impossible_title
import xyz.sevive.arcaeaoffline.resources.play_result_validator_score_is_zero_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_score_is_zero_title
import xyz.sevive.arcaeaoffline.resources.play_result_validator_score_out_of_range_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_score_out_of_range_title
import xyz.sevive.arcaeaoffline.resources.play_result_validator_warning_count

internal fun PlayResultWarning.titleRes(): StringResource =
    when (this) {
        PlayResultWarning.SCORE_IS_ZERO -> Res.string.play_result_validator_score_is_zero_title
        PlayResultWarning.PURE_MEMORY_FAR_LOST_NOT_ZERO -> Res.string.play_result_validator_pure_memory_far_lost_not_zero_title
        PlayResultWarning.ALL_PURE_NOT_PM -> Res.string.play_result_validator_all_pure_not_pm_title
        PlayResultWarning.SCORE_OUT_OF_RANGE -> Res.string.play_result_validator_score_out_of_range_title
        PlayResultWarning.SCORE_IMPOSSIBLE -> Res.string.play_result_validator_score_impossible_title
        PlayResultWarning.PM_SCORE_TOO_LOW -> Res.string.play_result_validator_pm_score_too_low_title
        PlayResultWarning.PFL_OVERFLOW -> Res.string.play_result_validator_pfl_overflow_title
        PlayResultWarning.MAX_RECALL_OVERFLOW -> Res.string.play_result_validator_max_recall_overflow_title
        PlayResultWarning.NEGATIVE_COUNT -> Res.string.play_result_validator_negative_count_title
        PlayResultWarning.FR_PM_MAX_RECALL_MISMATCH -> Res.string.play_result_validator_fr_pm_max_recall_mismatch_title
        PlayResultWarning.FULL_RECALL_LOST_NOT_ZERO -> Res.string.play_result_validator_full_recall_lost_not_zero_title
        PlayResultWarning.PLAIN_CLEAR_WITHOUT_LOST -> Res.string.play_result_validator_plain_clear_without_lost_title
        PlayResultWarning.CLEAR_PFL_MISMATCH -> Res.string.play_result_validator_clear_pfl_mismatch_title
        PlayResultWarning.MODIFIER_CLEAR_TYPE_MISMATCH -> Res.string.play_result_validator_modifier_clear_type_mismatch_title
        PlayResultWarning.CLEAR_TYPE_MISSING -> Res.string.play_result_validator_clear_type_missing_title
    }

internal fun PlayResultWarning.messageRes(): StringResource =
    when (this) {
        PlayResultWarning.SCORE_IS_ZERO -> Res.string.play_result_validator_score_is_zero_message
        PlayResultWarning.PURE_MEMORY_FAR_LOST_NOT_ZERO -> Res.string.play_result_validator_pure_memory_far_lost_not_zero_message
        PlayResultWarning.ALL_PURE_NOT_PM -> Res.string.play_result_validator_all_pure_not_pm_message
        PlayResultWarning.SCORE_OUT_OF_RANGE -> Res.string.play_result_validator_score_out_of_range_message
        PlayResultWarning.SCORE_IMPOSSIBLE -> Res.string.play_result_validator_score_impossible_message
        PlayResultWarning.PM_SCORE_TOO_LOW -> Res.string.play_result_validator_pm_score_too_low_message
        PlayResultWarning.PFL_OVERFLOW -> Res.string.play_result_validator_pfl_overflow_message
        PlayResultWarning.MAX_RECALL_OVERFLOW -> Res.string.play_result_validator_max_recall_overflow_message
        PlayResultWarning.NEGATIVE_COUNT -> Res.string.play_result_validator_negative_count_message
        PlayResultWarning.FR_PM_MAX_RECALL_MISMATCH -> Res.string.play_result_validator_fr_pm_max_recall_mismatch_message
        PlayResultWarning.FULL_RECALL_LOST_NOT_ZERO -> Res.string.play_result_validator_full_recall_lost_not_zero_message
        PlayResultWarning.PLAIN_CLEAR_WITHOUT_LOST -> Res.string.play_result_validator_plain_clear_without_lost_message
        PlayResultWarning.CLEAR_PFL_MISMATCH -> Res.string.play_result_validator_clear_pfl_mismatch_message
        PlayResultWarning.MODIFIER_CLEAR_TYPE_MISMATCH -> Res.string.play_result_validator_modifier_clear_type_mismatch_message
        PlayResultWarning.CLEAR_TYPE_MISSING -> Res.string.play_result_validator_clear_type_missing_message
    }

@Composable
fun playResultWarningCountText(count: Int): String = pluralStringResource(Res.plurals.play_result_validator_warning_count, count, count)
