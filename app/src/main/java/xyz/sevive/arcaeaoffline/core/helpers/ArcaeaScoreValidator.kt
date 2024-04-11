package xyz.sevive.arcaeaoffline.core.helpers

import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreClearType
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreModifier
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import kotlin.math.floor


class ArcaeaScoreValidator {
    companion object {
        private fun calculateScoreRange(notes: Int, pure: Int, far: Int): IntRange {
            val singleNoteScore = 10000000.0 / notes

            val actualScore = floor(
                singleNoteScore * pure + singleNoteScore * 0.5 * far
            ).toInt()

            return actualScore..actualScore + pure
        }

        fun validateScore(score: Score, chartInfo: ChartInfo?): List<ArcaeaScoreValidatorWarning> {
            val warnings = mutableListOf<ArcaeaScoreValidatorWarning>()

            val far = score.far
            val lost = score.lost
            val clearType = score.clearType

            if (
                clearType == ArcaeaScoreClearType.PURE_MEMORY.value
                && (far != 0 || lost != 0)
            ) {
                warnings.add(ArcaeaScoreValidatorPureMemoryFarLostNotZeroWarning())
            }

            if (chartInfo == null) {
                return warnings.toList()
            }

            if (chartInfo.notes == null) {
                return warnings.toList()
            }

            val notes = chartInfo.notes
            val pure = score.pure
            val maxRecall = score.maxRecall
            val modifier = score.modifier

            if (pure != null && far != null) {
                val scoreRange = calculateScoreRange(notes, pure, far)
                if (!scoreRange.contains(score.score)) {
                    warnings.add(ArcaeaScoreValidateScoreOutOfRangeWarning())
                }
            }

            if (pure != null && far != null && lost != null && pure + far + lost > notes) {
                warnings.add(ArcaeaScoreValidatorPflOverflowWarning())
            }

            if (maxRecall != null && maxRecall > notes) {
                warnings.add(ArcaeaScoreValidatorMaxRecallOverflowWarning())
            }

            if (
                clearType != null && modifier != null
                // is not hard lost
                && !(clearType == ArcaeaScoreClearType.TRACK_LOST.value && modifier == ArcaeaScoreModifier.HARD.value)
                && pure != null && far != null && lost != null
                && pure + far + lost != notes
            ) {
                warnings.add(ArcaeaScoreValidatorClearPflMismatchWarning())
            }

            return warnings.toList()
        }
    }
}
