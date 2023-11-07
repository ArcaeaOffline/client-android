package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey


@Entity(
    tableName = "packs_localized", foreignKeys = [ForeignKey(
        entity = Pack::class,
        parentColumns = ["id"],
        childColumns = ["id"],
        onUpdate = ForeignKey.NO_ACTION,
        deferred = true
    )]
)
data class PackLocalized(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "name_ja") val nameJa: String?,
    @ColumnInfo(name = "name_ko") val nameKo: String?,
    @ColumnInfo(name = "name_zh_hans") val nameZhHans: String?,
    @ColumnInfo(name = "name_zh_hant") val nameZhHant: String?,
    @ColumnInfo(name = "description_ja") val descriptionJa: String?,
    @ColumnInfo(name = "description_ko") val descriptionKo: String?,
    @ColumnInfo(name = "description_zh_hans") val descriptionZhHans: String?,
    @ColumnInfo(name = "description_zh_hant") val descriptionZhHant: String?,
)
