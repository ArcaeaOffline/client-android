package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import kotlin.uuid.Uuid

data class MinimumPlayResultPotentialFields(
    val uuid: Uuid,
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaRatingClass,
    val score: Int,
    val constant: Int,
)
