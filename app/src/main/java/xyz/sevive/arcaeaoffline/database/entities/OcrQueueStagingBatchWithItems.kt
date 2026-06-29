package xyz.sevive.arcaeaoffline.database.entities

import androidx.room.Embedded
import androidx.room.Relation

data class OcrQueueStagingBatchWithItems(
    @Embedded val batch: OcrQueueStagingBatch,
    @Relation(
        parentColumn = "id",
        entityColumn = "batch_id",
    )
    val items: List<OcrQueueStagingItem>,
)
