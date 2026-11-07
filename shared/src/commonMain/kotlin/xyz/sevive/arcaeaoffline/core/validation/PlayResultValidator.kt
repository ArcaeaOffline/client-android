package xyz.sevive.arcaeaoffline.core.validation

import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

/** A warning raised for a play result, with the values it compared. */
data class PlayResultFinding(
    val warning: PlayResultWarning,
    val detail: PlayResultWarningDetail = PlayResultWarningDetail.None,
)

object PlayResultValidator {
    /**
     * Warnings raised for [playResult] and [chartInfo], in declaration order. A null
     * [scoringMode] runs the general validation, which leaves out B50-only warnings.
     */
    fun validate(
        playResult: PlayResult,
        chartInfo: ChartInfo? = null,
        scoringMode: ArcaeaScoringMode? = null,
    ): List<PlayResultFinding> =
        PlayResultWarning.entries
            .filter { it.scope.appliesTo(scoringMode) }
            .mapNotNull { warning ->
                warning.check(playResult, chartInfo)?.let { detail ->
                    PlayResultFinding(warning, detail)
                }
            }
}
