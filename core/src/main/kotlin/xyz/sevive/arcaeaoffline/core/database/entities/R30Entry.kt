package xyz.sevive.arcaeaoffline.core.database.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import java.util.UUID

@Entity(
    tableName = "r30_entries",
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

data class R30EntryAndPlayResult(
    @Embedded val r30Entry: R30Entry,
    @Relation(parentColumn = "uuid", entityColumn = "uuid") val playResult: PlayResult,
) {
    companion object
}
