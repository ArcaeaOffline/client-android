package xyz.sevive.arcaeaoffline.core.database.entities

import kotlin.math.floor

data class PlayResultCalculated(
    val playResult: PlayResult,
    val chartInfo: ChartInfo,
) {
    val id = playResult.id
    val uuid = playResult.uuid
    val songId = playResult.songId
    val ratingClass = playResult.ratingClass
    val score = playResult.score
    val pure = playResult.pure
    val far = playResult.far
    val lost = playResult.lost
    val date = playResult.date
    val maxRecall = playResult.maxRecall
    val modifier = playResult.modifier
    val clearType = playResult.clearType
    val comment = playResult.comment

    val potential = calculatePotential(score = score, constant = chartInfo.constant)
    val shinyPure =
        if (chartInfo.notes == null || chartInfo.notes == 0 || pure == null || far == null || lost == null) {
            null
        } else {
            (score - floor((pure * 10000000.0 / chartInfo.notes) + (far * 0.5 * 100000000.0 / chartInfo.notes))).toInt()
        }
}
