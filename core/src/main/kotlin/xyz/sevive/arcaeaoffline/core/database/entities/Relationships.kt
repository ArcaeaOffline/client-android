package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import java.util.UUID

data class PlayResultWithChart(
    val playResult: PlayResult,
    val chart: Chart? = null,
)

data class PlayResultBestWithChart(
    val playResultBest: PlayResultCalculated,
    val chart: Chart? = null,
)

data class MinimumPlayResultPotentialFields(
    val uuid: UUID,
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaRatingClass,
    val score: Int,
    val constant: Int,
)
