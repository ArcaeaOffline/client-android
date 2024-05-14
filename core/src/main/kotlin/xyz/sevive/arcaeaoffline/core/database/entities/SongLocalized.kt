package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "songs_localized", foreignKeys = [ForeignKey(
        entity = Song::class,
        parentColumns = ["id"],
        childColumns = ["id"],
        onUpdate = ForeignKey.NO_ACTION,
        deferred = true
    )]
)
data class SongLocalized(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "title_ja") val titleJa: String?,
    @ColumnInfo(name = "title_ko") val titleKo: String?,
    @ColumnInfo(name = "title_zh_hans") val titleZhHans: String?,
    @ColumnInfo(name = "title_zh_hant") val titleZhHant: String?,
    @ColumnInfo(name = "search_title_ja") val searchTitleJa: String?,
    @ColumnInfo(name = "search_title_ko") val searchTitleKo: String?,
    @ColumnInfo(name = "search_title_zh_hans") val searchTitleZhHans: String?,
    @ColumnInfo(name = "search_title_zh_hant") val searchTitleZhHant: String?,
    @ColumnInfo(name = "search_artist_ja") val searchArtistJa: String?,
    @ColumnInfo(name = "search_artist_ko") val searchArtistKo: String?,
    @ColumnInfo(name = "search_artist_zh_hans") val searchArtistZhHans: String?,
    @ColumnInfo(name = "search_artist_zh_hant") val searchArtistZhHant: String?,
    @ColumnInfo(name = "source_title_ja") val sourceTitleJa: String?,
    @ColumnInfo(name = "source_title_ko") val sourceTitleKo: String?,
    @ColumnInfo(name = "source_title_zh_hans") val sourceTitleZhHans: String?,
    @ColumnInfo(name = "source_title_zh_hant") val sourceTitleZhHant: String?,
)
