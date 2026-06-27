package xyz.sevive.arcaeaoffline.database.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ocr_queue_enqueue_buffer",
    indices = [Index(value = ["uri"], unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = OcrQueueEnqueueBatch::class,
            parentColumns = ["id"],
            childColumns = ["batch_id"],
            onUpdate = ForeignKey.CASCADE,
            onDelete = ForeignKey.CASCADE,
        ),
    ],
)
data class OcrQueueEnqueueBuffer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "batch_id")
    val batchId: Long,
    @ColumnInfo(name = "uri")
    val uri: Uri,
    @ColumnInfo(name = "uri_type")
    val uriType: OcrQueueUriType,
    @ColumnInfo(name = "checked")
    val checked: Boolean = false,
    @ColumnInfo(name = "should_insert")
    val shouldInsert: Boolean = false,
)
