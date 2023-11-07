package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "scores")
data class Score(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: Int,
    val score: Int,
    val pure: Int?,
    val far: Int?,
    val lost: Int?,
    val date: Int?,
    @ColumnInfo(name = "max_recall") val maxRecall: Int?,
    val modifier: Int?,
    @ColumnInfo(name = "clear_type") val clearType: Int?,
    val comment: String?
)
