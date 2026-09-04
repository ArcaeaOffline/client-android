package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass

data class Chart(
    @ColumnInfo(name = "song_idx") val songIdx: Int,
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: ArcaeaRatingClass,
    @ColumnInfo(name = "rating_class_alias") val ratingClassAlias: Int? = null,
    val rating: Int,
    @ColumnInfo(name = "rating_plus") val ratingPlus: Boolean,
    val title: String,
    val artist: String,
    val set: String,
    val bpm: String? = null,
    @ColumnInfo(name = "bpm_base") val bpmBase: Double? = null,
    @ColumnInfo(name = "audio_preview") val audioPreview: Int? = null,
    @ColumnInfo(name = "audio_preview_end") val audioPreviewEnd: Int? = null,
    val side: Int,
    val version: String? = null,
    val date: Int? = null,
    val bg: String? = null,
    @ColumnInfo(name = "bg_inverse") val bgInverse: String? = null,
    @ColumnInfo(name = "bg_day") val bgDay: String? = null,
    @ColumnInfo(name = "bg_night") val bgNight: String? = null,
    val source: String? = null,
    @ColumnInfo(name = "source_copyright") val sourceCopyright: String? = null,
    @ColumnInfo(name = "chart_designer") val chartDesigner: String? = null,
    @ColumnInfo(name = "jacket_designer") val jacketDesigner: String? = null,
    @ColumnInfo(name = "audio_override") val audioOverride: Boolean,
    @ColumnInfo(name = "jacket_override") val jacketOverride: Boolean,
    @ColumnInfo(name = "jacket_night") val jacketNight: String? = null,
    val constant: Int,
    val notes: Int? = null,
)
