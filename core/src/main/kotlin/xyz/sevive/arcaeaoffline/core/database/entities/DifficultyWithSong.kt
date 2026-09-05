package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Embedded
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass

// Pure songlist-view display model: rating metadata from difficulties joined
// with song metadata, COALESCE resolved in SQL. Constant-bearing chart info
// lives in ChartInfo and is deliberately not part of this model, so display
// keeps working while the external chart info database lags behind songlist.
data class DifficultyWithSong(
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaRatingClass,
    @ColumnInfo(name = "rating_class_alias") val ratingClassAlias: Int? = null,
    val rating: Int,
    @ColumnInfo(name = "rating_plus") val ratingPlus: Boolean,
    val title: String,
    val artist: String,
)

// Same shape as [DifficultyWithSong] plus the chart info columns; produced by
// joined queries for consumers that need the constant (recommend, calculator).
data class DifficultyWithSongAndInfo(
    @Embedded val difficultyWithSong: DifficultyWithSong,
    val constant: Int,
    val notes: Int?,
)
