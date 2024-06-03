package xyz.sevive.arcaeaoffline.core.database.entities

data class PlayResultWithChart(
    val playResult: PlayResult,
    val chart: Chart? = null,
)

data class PlayResultBestWithChart(
    val playResultBest: PlayResultBest,
    val chart: Chart? = null,
)
