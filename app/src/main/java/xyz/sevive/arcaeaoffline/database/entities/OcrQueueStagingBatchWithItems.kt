package xyz.sevive.arcaeaoffline.database.entities

import androidx.room3.Embedded
import androidx.room3.Relation

data class OcrQueueStagingBatchWithItems(
    @Embedded val batch: OcrQueueStagingBatch,
    @Relation(
        parentColumns = ["id"],
        entityColumns = ["batch_id"],
    )
    val items: List<OcrQueueStagingItem>,
)
