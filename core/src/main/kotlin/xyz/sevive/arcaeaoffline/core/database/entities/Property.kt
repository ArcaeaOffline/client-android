package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "properties")
data class Property(
    @PrimaryKey val key: String,
    val value: String,
) {
    companion object {
        const val KEY_R30_LAST_UPDATED_AT = "r30_last_updated_at"
        const val KEY_R30_LAST_UPDATED_PLAY_RESULT_UUID = "r30_last_updated_uuid"
    }
}
