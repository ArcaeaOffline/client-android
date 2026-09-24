package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass

/** Highest score of a chart, as of a point in the play history. */
data class ChartBestScore(
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaRatingClass,
    val score: Int,
)
