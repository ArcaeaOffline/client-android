package xyz.sevive.arcaeaoffline.ui.screens.database.manage

import androidx.annotation.PluralsRes
import androidx.annotation.StringRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.Uuid

internal sealed interface ImportLogEvent {
    data class Plural(
        @PluralsRes val resId: Int,
        val quantity: Int,
    ) : ImportLogEvent

    data class SimpleString(
        @StringRes val resId: Int,
    ) : ImportLogEvent

    data class Raw(
        val message: String,
    ) : ImportLogEvent
}

internal data class ImportLogObject(
    val uuid: Uuid = Uuid.generateV4(),
    val timestamp: Instant,
    val tag: String? = null,
    val event: ImportLogEvent,
)

internal class ImportLogManager {
    private val lock = Mutex()
    private val _logs = MutableStateFlow(emptyList<ImportLogObject>())
    val logs: StateFlow<List<ImportLogObject>> = _logs

    suspend fun append(
        tag: String?,
        event: ImportLogEvent,
    ) {
        lock.withLock {
            _logs.value += ImportLogObject(timestamp = Clock.System.now(), tag = tag, event = event)
        }
    }
}
