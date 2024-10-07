package xyz.sevive.arcaeaoffline.helpers

import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult


object ArcaeaPlayResultValidator {
    val WARNINGS = listOf(
        ArcaeaPlayResultValidatorScoreIsZeroWarning,
        ArcaeaPlayResultValidatorPureMemoryFarLostNotZeroWarning,
        ArcaeaPlayResultValidatorScoreOutOfRangeWarning,
        ArcaeaPlayResultValidatorPflOverflowWarning,
        ArcaeaPlayResultValidatorMaxRecallOverflowWarning,
        ArcaeaPlayResultValidatorFrPmMaxRecallMismatchWarning,
        ArcaeaPlayResultValidatorFullRecallLostNotZeroWarning,
        ArcaeaPlayResultValidatorClearPflMismatchWarning,
        ArcaeaPlayResultValidatorModifierClearTypeMismatchWarning,
    )

    fun validate(
        playResult: PlayResult,
        chartInfo: ChartInfo?
    ): List<ArcaeaPlayResultValidatorWarning> {
        val warnings = mutableListOf<ArcaeaPlayResultValidatorWarning>()

        for (warning in WARNINGS) {
            if (warning.conditionsMet(playResult, chartInfo)) {
                warnings.add(warning)
            }
        }

        return warnings.toList()
    }

    fun validate(
        playResult: PlayResult,
        chart: Chart?
    ): List<ArcaeaPlayResultValidatorWarning> {
        if (chart == null) {
            return validate(playResult = playResult, chartInfo = null)
        }

        return validate(
            playResult = playResult,
            chartInfo = ChartInfo(
                songId = chart.songId,
                ratingClass = chart.ratingClass,
                constant = chart.constant,
                notes = chart.notes,
            )
        )
    }
}
