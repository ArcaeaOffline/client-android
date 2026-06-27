package xyz.sevive.arcaeaoffline.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.Instant

@Entity(tableName = "ocr_queue_enqueue_batches")
data class OcrQueueEnqueueBatch(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "inserted_at")
    val insertedAt: Instant,
    @ColumnInfo(name = "options")
    val options: OcrQueueEnqueueOptions,
)
