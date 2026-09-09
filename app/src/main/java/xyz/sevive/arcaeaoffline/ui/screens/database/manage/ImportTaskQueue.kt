package xyz.sevive.arcaeaoffline.ui.screens.database.manage

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.uuid.Uuid

/**
 * Serial task queue: tasks run one at a time in submission order, never concurrently.
 *
 * Any [Throwable] thrown inside a task is caught and written to [ImportLogManager] (except
 * [CancellationException], which is rethrown: cancellation is not a task failure); the queue
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

    // Consumer and tasks anchor to parentScope's Job: cancelling parentScope cancels queued and running tasks.
    private val parentJob =
        checkNotNull(parentScope.coroutineContext[Job]) { "parentScope must carry a Job in its context" }

    private val taskScope =
        CoroutineScope(SupervisorJob(parentJob) + Dispatchers.IO)
    private val taskChannel = Channel<Task>(Channel.UNLIMITED)

    private val pendingTaskCount = MutableStateFlow(0)

    /** True from the moment a task is submitted until it settles, so the UI never flickers between queued tasks. */
    val isWorking: Flow<Boolean> = pendingTaskCount.map { it > 0 }

    init {
        parentScope.launch(Dispatchers.Default) {
            taskChannel.consumeEach { task ->
                logger.d { "Processing task ${task.uuid}" }
                taskScope
                    .launch {
                        try {
                            task.action(this)
                        } catch (e: CancellationException) {
                            // Rethrow: the append below would itself throw on the cancelled coroutine,
                            // and cancellation must not be logged as a task failure.
                            throw e
                        } catch (e: Throwable) {
                            logger.e(e) { "Error processing task ${task.uuid}" }
                            importLogManager.append(
                                tag = null,
                                event = ImportLogEvent.Raw(e.toString()),
                            )
                        }
                    }.join()
                pendingTaskCount.update { it - 1 }
            }
        }
    }

    suspend fun send(action: suspend CoroutineScope.() -> Unit) {
        pendingTaskCount.update { it + 1 }
        val task = Task(action = action)
        taskChannel.send(task)
        logger.d { "Task ${task.uuid} sent" }
    }
}
