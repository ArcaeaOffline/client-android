package xyz.sevive.arcaeaoffline.helpers

import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

object ArcaeaPlayResultValidator {
    val WARNINGS =
        listOf(
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
        chartInfo: ChartInfo?,
    ): List<ArcaeaPlayResultValidatorWarning> {
        val warnings = mutableListOf<ArcaeaPlayResultValidatorWarning>()

        for (warning in WARNINGS) {
            if (warning.conditionsMet(playResult, chartInfo)) {
                warnings.add(warning)
            }
        }

        return warnings.toList()
    }

    /**
     * Display-oriented validation: on top of [validate], flags records whose
     * missing clear type changes their B50 rating, so the user can fill it in.
     */
    fun validate(
        playResult: PlayResult,
        chartInfo: ChartInfo?,
        scoringMode: ArcaeaScoringMode,
    ): List<ArcaeaPlayResultValidatorWarning> {
        val warnings = validate(playResult, chartInfo).toMutableList()

        if (
            scoringMode == ArcaeaScoringMode.B50 &&
            ArcaeaPlayResultValidatorClearTypeMissingWarning.conditionsMet(playResult, chartInfo)
        ) {
            warnings.add(ArcaeaPlayResultValidatorClearTypeMissingWarning)
        }

        return warnings
    }
}
