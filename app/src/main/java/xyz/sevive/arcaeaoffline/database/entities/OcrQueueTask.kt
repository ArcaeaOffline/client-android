package xyz.sevive.arcaeaoffline.database.entities

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult
import kotlin.time.Instant

enum class OcrQueueTaskStatus(
    val value: Int,
) {
    IDLE(0),
    ERROR(1),
    PROCESSING(2),
    DONE(3),
}

@Entity(
    tableName = "ocr_queue_tasks",
    indices = [Index("file_uri", unique = true)],
)
data class OcrQueueTask(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "inserted_at") val insertedAt: Instant,
    @ColumnInfo(name = "file_uri") val fileUri: Uri,
    @ColumnInfo(name = "status") val status: OcrQueueTaskStatus = OcrQueueTaskStatus.IDLE,
    @ColumnInfo(name = "result") val result: DeviceOcrResult? = null,
    @ColumnInfo(name = "play_result") val playResult: PlayResult? = null,
    @ColumnInfo(name = "error_type") val errorType: String? = null,
    @ColumnInfo(name = "error_message") val errorMessage: String? = null,
)
