package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass


@Entity(tableName = "difficulties", primaryKeys = ["song_id", "rating_class"])
data class Difficulty(
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaRatingClass,
    val rating: Int,
    @ColumnInfo(name = "rating_plus") val ratingPlus: Boolean,
    @ColumnInfo(name = "chart_designer") val chartDesigner: String?,
    @ColumnInfo(name = "jacket_designer") val jacketDesigner: String?,
    @ColumnInfo(name = "audio_override") val audioOverride: Boolean,
    @ColumnInfo(name = "jacket_override") val jacketOverride: Boolean,
    @ColumnInfo(name = "jacket_night") val jacketNight: String?,
    val title: String?,
    val artist: String?,
    val bg: String?,
    @ColumnInfo(name = "bg_inverse") val bgInverse: String?,
    val bpm: String?,
    @ColumnInfo(name = "bpm_base") val bpmBase: Double?,
    val version: String?,
    val date: Int?
)
