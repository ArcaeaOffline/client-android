package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "properties")
data class Property(
    @PrimaryKey val key: String,
    val value: String,
)
