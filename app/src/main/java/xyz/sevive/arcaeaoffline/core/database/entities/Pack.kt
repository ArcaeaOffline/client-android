package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "packs")
data class Pack(
    @PrimaryKey val id: String,
    val name: String,
    val description: String? = null,
)
