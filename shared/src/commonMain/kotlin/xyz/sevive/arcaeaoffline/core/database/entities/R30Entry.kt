package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey
import kotlin.uuid.Uuid

@Entity(
    tableName = "r30_entries",
    indices = [Index("uuid")],
    foreignKeys = [
        ForeignKey(
            entity = PlayResult::class,
            parentColumns = ["uuid"],
            childColumns = ["uuid"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
            deferred = true,
        ),
    ],
)
data class R30Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: Uuid,
) {
    companion object
}
