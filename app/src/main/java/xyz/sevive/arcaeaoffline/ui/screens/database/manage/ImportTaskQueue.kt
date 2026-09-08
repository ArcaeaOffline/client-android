package xyz.sevive.arcaeaoffline.ui.screens.database.manage

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

/**
 * Serial task queue: tasks run one at a time in submission order, never concurrently.
 *
 * Any [Throwable] thrown inside a task is caught and written to [ImportLogManager]; the queue
 * keeps processing later tasks. [taskScope] is a child of [parentScope]'s Job, so cancelling
 * parentScope (e.g. ViewModel destruction) cancels both queued and running tasks.
 */
internal class ImportTaskQueue(
    parentScope: CoroutineScope,
    private val importLogManager: ImportLogManager,
    private val logger: Logger,
) {
    private data class Task(
        val uuid: Uuid = Uuid.generateV4(),
        val action: suspend CoroutineScope.() -> Unit,
    )

    private val taskScope =
        CoroutineScope(SupervisorJob(parentScope.coroutineContext[Job]!!) + Dispatchers.IO)
    private val taskChannel = Channel<Task>(Channel.UNLIMITED)

    private val _isWorking = MutableStateFlow(false)
    val isWorking: StateFlow<Boolean> = _isWorking.asStateFlow()

    init {
        parentScope.launch(Dispatchers.Default) {
            taskChannel.consumeEach { task ->
                _isWorking.value = true
                logger.d { "Processing task ${task.uuid}" }
                taskScope
                    .launch {
                        try {
                            task.action(this)
                        } catch (e: Throwable) {
                            logger.e(e) { "Error processing task ${task.uuid}" }
                            importLogManager.append(
                                tag = null,
                                event = ImportLogEvent.Raw(e.toString()),
                            )
                        }
                    }.join()
                _isWorking.value = false
            }
        }
    }

    suspend fun send(action: suspend CoroutineScope.() -> Unit) {
        val task = Task(action = action)
        taskChannel.send(task)
        logger.d { "Task ${task.uuid} sent" }
    }
}
