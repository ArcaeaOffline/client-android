package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass


@Entity(
    tableName = "charts_info",
    primaryKeys = ["song_id", "rating_class"],
)
data class ChartInfo(
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaRatingClass,
    val constant: Int,
    val notes: Int?,
)
