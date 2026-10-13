package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaLanguage

@Entity(
    tableName = "songs_localized",
    indices = [
        Index(value = ["id", "lang"], unique = true),
        Index(value = ["lang"]),
    ],
    foreignKeys = [
        ForeignKey(
            entity = Song::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
            deferred = true,
        ),
    ],
)
data class SongLocalized(
    @PrimaryKey val id: String,
    val lang: ArcaeaLanguage,
    val title: String?,
    @ColumnInfo(name = "source") val source: String?,
)
