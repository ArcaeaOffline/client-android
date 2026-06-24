package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

val appendPackRegexPattern = """(.*)_append_.*$""".toRegex()

@Entity(tableName = "packs")
data class Pack(
    @PrimaryKey val id: String,
    val name: String,
    val description: String? = null,
) {
    fun basePackId(): String? = appendPackRegexPattern.matchEntire(this.id)?.groupValues?.getOrNull(1)

    fun isAppendPack(): Boolean = basePackId() != null
}
