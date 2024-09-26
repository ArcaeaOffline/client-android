package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaLanguage


@Entity(
    tableName = "packs_localized",
    indices = [
        Index(value = ["id", "lang"], unique = true),
        Index(value = ["lang"])
    ],
    foreignKeys = [ForeignKey(
        entity = Pack::class,
        parentColumns = ["id"],
        childColumns = ["id"],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE,
        deferred = true,
    )],
)
data class PackLocalized(
    @PrimaryKey val id: String,
    val lang: ArcaeaLanguage,
    val name: String?,
    val description: String?,
)
