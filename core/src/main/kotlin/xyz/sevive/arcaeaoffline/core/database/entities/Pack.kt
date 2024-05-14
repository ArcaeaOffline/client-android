package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

val appendPackRegex = """(?<basePackId>.*)_append_.*$""".toRegex()

@Entity(tableName = "packs")
data class Pack(
    @PrimaryKey val id: String,
    val name: String,
    val description: String? = null,
) {
    private fun appendPackRegexFindResult(): MatchResult? {
        return appendPackRegex.find(this.id)
    }

    fun isAppendPack(): Boolean {
        return appendPackRegexFindResult() != null
    }

    fun basePackId(): String? {
        val match = appendPackRegexFindResult() ?: return null
        return match.groups["basePackId"]?.value
    }
}
