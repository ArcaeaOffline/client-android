package xyz.sevive.arcaeaoffline.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class OcrQueueBatchWithBuffers(
    @Embedded val batch: OcrQueueEnqueueBatch,
    @Relation(
        parentColumn = "id",
        entityColumn = "batch_id",
    )
    val buffers: List<OcrQueueEnqueueBuffer>,
)
