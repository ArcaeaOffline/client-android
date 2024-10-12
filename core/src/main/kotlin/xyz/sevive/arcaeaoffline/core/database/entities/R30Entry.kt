package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "r30_entries",
    indices = [Index("uuid")],
    foreignKeys = [ForeignKey(
        entity = PlayResult::class,
        parentColumns = ["uuid"],
        childColumns = ["uuid"],
        onUpdate = ForeignKey.CASCADE,
        onDelete = ForeignKey.CASCADE,
        deferred = true,
    )],
)
data class R30Entry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val uuid: UUID,
) {
    companion object
}
