package xyz.sevive.arcaeaoffline.ui.components.playresult

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import xyz.sevive.arcaeaoffline.core.validation.PlayResultFinding
import xyz.sevive.arcaeaoffline.core.validation.PlayResultWarning
import xyz.sevive.arcaeaoffline.core.validation.PlayResultWarningDetail
import xyz.sevive.arcaeaoffline.resources.Res
import xyz.sevive.arcaeaoffline.resources.play_result_validator_all_pure_not_pm_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_clear_pfl_mismatch_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_clear_type_missing_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_fr_pm_max_recall_mismatch_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_full_recall_lost_not_zero_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_max_recall_overflow_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_modifier_clear_type_mismatch_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_negative_count_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_pfl_overflow_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_pfl_overflow_message_detailed
import xyz.sevive.arcaeaoffline.resources.play_result_validator_plain_clear_without_lost_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_pm_score_too_low_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_pure_memory_far_lost_not_zero_message
import xyz.sevive.arcaeaoffline.resources.play_result_validator_score_impossible_message_beyond_max
import xyz.sevive.arcaeaoffline.resources.play_result_validator_score_impossible_message_negative
import xyz.sevive.arcaeaoffline.resources.play_result_validator_score_out_of_range_message
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters

private fun formatScore(score: Int): String = if (score < 0) score.toString() else ArcaeaFormatters.score(score)

@Composable
internal fun PlayResultFinding.messageText(): String? =
    when (warning) {
        PlayResultWarning.EMPTY_PLAY_RESULT -> {
            null
        }

        PlayResultWarning.PURE_MEMORY_FAR_LOST_NOT_ZERO -> {
            stringResource(Res.string.play_result_validator_pure_memory_far_lost_not_zero_message)
        }

        PlayResultWarning.ALL_PURE_NOT_PM -> {
            stringResource(Res.string.play_result_validator_all_pure_not_pm_message)
        }

        PlayResultWarning.SCORE_OUT_OF_RANGE -> {
            when (val d = detail) {
                is PlayResultWarningDetail.ScoreOutOfRange -> {
                    stringResource(
                        Res.string.play_result_validator_score_out_of_range_message,
                        formatScore(d.actual),
                        formatScore(d.expected.first),
                        formatScore(d.expected.last),
                        d.pure,
                        d.far,
                    )
                }

                else -> {
                    null
                }
            }
        }

        PlayResultWarning.SCORE_IMPOSSIBLE -> {
            when (val d = detail) {
                is PlayResultWarningDetail.ScoreNegative -> {
                    stringResource(
                        Res.string.play_result_validator_score_impossible_message_negative,
                        d.actual,
                    )
                }

                is PlayResultWarningDetail.ScoreBeyondMaximum -> {
                    stringResource(
                        Res.string.play_result_validator_score_impossible_message_beyond_max,
                        formatScore(d.actual),
                        formatScore(d.maximum),
                    )
                }

                else -> {
                    null
                }
            }
        }

        PlayResultWarning.PM_SCORE_TOO_LOW -> {
            when (val d = detail) {
                is PlayResultWarningDetail.ScoreTooLow -> {
                    stringResource(
                        Res.string.play_result_validator_pm_score_too_low_message,
                        formatScore(d.actual),
                    )
                }

                else -> {
                    null
                }
            }
        }

        PlayResultWarning.PFL_OVERFLOW -> {
            when (val d = detail) {
                is PlayResultWarningDetail.Judgments -> {
                    stringResource(
                        Res.string.play_result_validator_pfl_overflow_message_detailed,
                        d.pure,
                        d.far,
                        d.lost,
                        d.notes,
                    )
                }

                else -> {
                    stringResource(Res.string.play_result_validator_pfl_overflow_message)
                }
            }
        }

        PlayResultWarning.MAX_RECALL_OVERFLOW -> {
            when (val d = detail) {
                is PlayResultWarningDetail.MaxRecall -> {
                    stringResource(
                        Res.string.play_result_validator_max_recall_overflow_message,
                        d.actual,
                        d.expected,
                    )
                }

                else -> {
                    null
                }
            }
        }

        PlayResultWarning.NEGATIVE_COUNT -> {
            stringResource(Res.string.play_result_validator_negative_count_message)
        }

        PlayResultWarning.FR_PM_MAX_RECALL_MISMATCH -> {
            when (val d = detail) {
                is PlayResultWarningDetail.MaxRecall -> {
                    stringResource(
                        Res.string.play_result_validator_fr_pm_max_recall_mismatch_message,
                        d.actual,
                        d.expected,
                    )
                }

                else -> {
                    null
                }
            }
        }

        PlayResultWarning.FULL_RECALL_LOST_NOT_ZERO -> {
            stringResource(Res.string.play_result_validator_full_recall_lost_not_zero_message)
        }

        PlayResultWarning.PLAIN_CLEAR_WITHOUT_LOST -> {
            stringResource(Res.string.play_result_validator_plain_clear_without_lost_message)
        }

        PlayResultWarning.CLEAR_PFL_MISMATCH -> {
            when (val d = detail) {
                is PlayResultWarningDetail.Judgments -> {
                    stringResource(
                        Res.string.play_result_validator_clear_pfl_mismatch_message,
                        d.pure,
                        d.far,
                        d.lost,
                        d.notes,
                    )
                }

                else -> {
                    null
                }
            }
        }

        PlayResultWarning.MODIFIER_CLEAR_TYPE_MISMATCH -> {
            stringResource(Res.string.play_result_validator_modifier_clear_type_mismatch_message)
        }

        PlayResultWarning.CLEAR_TYPE_MISSING -> {
            stringResource(Res.string.play_result_validator_clear_type_missing_message)
        }
    }
