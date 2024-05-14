package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass


@Entity(
    tableName = "charts_info", primaryKeys = ["song_id", "rating_class"], foreignKeys = [ForeignKey(
        entity = Difficulty::class,
        parentColumns = ["song_id", "rating_class"],
        childColumns = ["song_id", "rating_class"],
        onUpdate = ForeignKey.NO_ACTION,
        deferred = true
    )]
)
data class ChartInfo(
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaScoreRatingClass,
    val constant: Int,
    val notes: Int?,
)
