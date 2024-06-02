package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.regex.Matcher
import java.util.regex.Pattern

val appendPackRegexPattern: Pattern = Pattern.compile("""(.*)_append_.*${'$'}""")

@Entity(tableName = "packs")
data class Pack(
    @PrimaryKey val id: String,
    val name: String,
    val description: String? = null,
) {
    @Ignore
    private val appendPackRegexMatcher: Matcher = appendPackRegexPattern.matcher(this.id)

    fun basePackId(): String? {
        return if (appendPackRegexMatcher.matches()) {
            appendPackRegexMatcher.group(1)
        } else null
    }

    fun isAppendPack(): Boolean {
        return basePackId() != null
    }
}
