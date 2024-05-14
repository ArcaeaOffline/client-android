package xyz.sevive.arcaeaoffline.helpers

import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.Score


class ArcaeaScoreValidator {
    companion object {
        private val WARNINGS = listOf(
            ArcaeaScoreValidatorPureMemoryFarLostNotZeroWarning(),
            ArcaeaScoreValidateScoreOutOfRangeWarning(),
            ArcaeaScoreValidatorPflOverflowWarning(),
            ArcaeaScoreValidatorMaxRecallOverflowWarning(),
            ArcaeaScoreValidatorFrPmMaxRecallMismatchWarning(),
            ArcaeaScoreValidatorFullRecallLostNotZeroWarning(),
            ArcaeaScoreValidatorClearPflMismatchWarning(),
            ArcaeaScoreValidatorModifierClearTypeMismatchWarning(),
        )

        fun validateScore(score: Score, chartInfo: ChartInfo?): List<ArcaeaScoreValidatorWarning> {
            val warnings = mutableListOf<ArcaeaScoreValidatorWarning>()

            for (warning in WARNINGS) {
                if (warning.conditionsMet(score, chartInfo)) {
                    warnings.add(warning)
                }
            }

            return warnings.toList()
        }

        fun validateScore(score: Score, chart: Chart?): List<ArcaeaScoreValidatorWarning> {
            if (chart == null) {
                return validateScore(score = score, chartInfo = null)
            }

            return validateScore(
                score = score,
                chartInfo = ChartInfo(
                    songId = chart.songId,
                    ratingClass = chart.ratingClass,
                    constant = chart.constant,
                    notes = chart.notes,
                )
            )
        }
    }
}
