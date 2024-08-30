package xyz.sevive.arcaeaoffline.database.entities

import android.content.Context
import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning
import xyz.sevive.arcaeaoffline.helpers.context.getFilename

enum class OcrQueueTaskStatus(val value: Int) { IDLE(0), ERROR(1), PROCESSING(2), DONE(3) }

@Entity(
    tableName = "ocr_queue_tasks",
    indices = [Index("file_uri", unique = true)]
)
data class OcrQueueTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "inserted_at") val insertedAt: Instant,

    @ColumnInfo(name = "file_uri") val fileUri: Uri,
    @ColumnInfo(name = "file_name") val fileName: String? = null,
    @ColumnInfo(name = "status") val status: OcrQueueTaskStatus = OcrQueueTaskStatus.IDLE,
    @ColumnInfo(name = "result") val result: DeviceOcrResult? = null,
    @ColumnInfo(name = "play_result") val playResult: PlayResult? = null,
    @ColumnInfo(name = "warnings") val warnings: List<ArcaeaPlayResultValidatorWarning>? = null,
    @ColumnInfo(name = "exception") val exception: Exception? = null,
) {
    constructor(uri: Uri, context: Context? = null) : this(
        fileUri = uri,
        insertedAt = Instant.now(),
        fileName = context?.getFilename(uri),
    )
}
