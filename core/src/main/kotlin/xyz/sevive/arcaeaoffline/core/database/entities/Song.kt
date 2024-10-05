package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "songs")
data class Song(
    val idx: Int,
    @PrimaryKey val id: String,
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
    @ColumnInfo(name = "deleted_in_game") val deletedInGame: Boolean? = null,
)
