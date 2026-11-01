package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

val appendPackRegexPattern = """(.*)_append_.*$""".toRegex()

@Entity(tableName = "packs")
data class Pack(
    @PrimaryKey val id: String,
    val name: String,
    val description: String? = null,
    @ColumnInfo(name = "parent_pack_id") val parentPackId: String? = null,
    val section: String? = null,
    @ColumnInfo(name = "plus_character_id") val plusCharacterId: Int? = null,
    @ColumnInfo(name = "is_extend_pack", defaultValue = "0") val isExtendPack: Boolean = false,
) {
    fun basePackId(): String? = appendPackRegexPattern.matchEntire(this.id)?.groupValues?.getOrNull(1)

    fun isAppendPack(): Boolean = basePackId() != null
}
