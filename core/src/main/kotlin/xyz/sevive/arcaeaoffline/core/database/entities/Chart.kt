package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.DatabaseView


@DatabaseView(
    """
        SELECT
            s.idx AS song_idx, d.song_id, d.rating_class, d.rating, d.rating_plus,
            COALESCE(d.title, s.title) AS title, COALESCE(d.artist, s.artist) AS artist,
            s.`set`, COALESCE(d.bpm, s.bpm) AS bpm, COALESCE(d.bpm_base, s.bpm_base) AS bpm_base,
            s.audio_preview, s.audio_preview_end, s.side,
            COALESCE(d.version, s.version) AS version, COALESCE(d.date, s.date) AS date,
            COALESCE(d.bg, s.bg) AS bg, COALESCE(d.bg_inverse, s.bg_inverse) AS bg_inverse,
            s.bg_day, s.bg_night, s.source, s.source_copyright,
            d.chart_designer, d.jacket_designer, d.audio_override, d.jacket_override,
            d.jacket_night, ci.constant, ci.notes
        FROM difficulties d
        INNER JOIN charts_info ci ON d.song_id = ci.song_id AND d.rating_class = ci.rating_class
        INNER JOIN songs s ON d.song_id = s.id
    """, "charts"
)
data class Chart(
    @ColumnInfo(name = "song_idx") val songIdx: Int,
    @ColumnInfo(name = "song_id") val songId: String,
    @ColumnInfo(name = "rating_class") val ratingClass: Int,
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
