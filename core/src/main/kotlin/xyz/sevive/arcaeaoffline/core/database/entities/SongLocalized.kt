package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "songs_localized",
    indices = [
        Index(value = ["id", "lang"], unique = true),
        Index(value = ["lang"]),
    ],
    foreignKeys = [ForeignKey(
        entity = Song::class,
        parentColumns = ["id"],
        childColumns = ["id"],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE,
        deferred = true,
    )],
)
data class SongLocalized(
    @PrimaryKey val id: String,
    val lang: String,
    val title: String?,
    @ColumnInfo(name = "search_title") val searchTitle: String?,
    @ColumnInfo(name = "search_artist") val searchArtist: String?,
    @ColumnInfo(name = "source_title") val sourceTitle: String?,
)
