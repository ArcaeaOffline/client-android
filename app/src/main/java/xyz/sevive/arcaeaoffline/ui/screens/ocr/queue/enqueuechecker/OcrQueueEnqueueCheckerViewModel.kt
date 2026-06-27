package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.enqueuechecker

import android.content.Context
import android.content.res.Resources
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.toAndroidUri
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.Progress
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueBatch
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueEnqueueOptions
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueUriType
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueEnqueueBatchRepository
import xyz.sevive.arcaeaoffline.database.repositories.OcrQueueEnqueueBufferRepository
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferences
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesSerializer
import xyz.sevive.arcaeaoffline.helpers.fromWorkInfo
import xyz.sevive.arcaeaoffline.jobs.OcrQueueEnqueueCheckerJob
import xyz.sevive.arcaeaoffline.jobs.OcrQueueJob
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class OcrQueueEnqueueCheckerViewModel(
    context: Context,
    private val bufferRepo: OcrQueueEnqueueBufferRepository,
    private val batchRepo: OcrQueueEnqueueBatchRepository,
    preferencesRepository: OcrQueuePreferencesRepository,
) : ViewModel() {
    private val workManager = WorkManager.getInstance(context.applicationContext)

    private val ocrQueuePreferences =
        preferencesRepository.preferencesFlow.stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            OcrQueuePreferencesSerializer.defaultValue,
        )

    // TODO: combine with ImportLog?
    sealed class UiEvent {
        data class StringRes(
            @androidx.annotation.StringRes val resId: Int,
            val formatArgs: List<Any> = emptyList(),
        ) : UiEvent()

        data class PluralRes(
            @androidx.annotation.PluralsRes val resId: Int,
            val quantity: Int,
            val formatArgs: List<Any> = emptyList(),
        ) : UiEvent()

        fun asString(resources: Resources): String =
            when (this) {
                is StringRes -> resources.getString(resId, *formatArgs.toTypedArray())
                is PluralRes -> resources.getQuantityString(resId, quantity, *formatArgs.toTypedArray())
            }
    }

    data class BufferItemsCount(
        val checked: Int = 0,
        val total: Int = 0,
    )

    data class UiState(
        val isEnqueueCheckerRunning: Boolean = false,
        val isOcrQueueRunning: Boolean = true,
        val workerProgress: Progress? = null,
        val bufferItemsCount: BufferItemsCount? = null,
    )

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val enqueueCheckerJobWorkInfoFlow =
        workManager.getWorkInfosForUniqueWorkFlow(OcrQueueEnqueueCheckerJob.WORK_NAME).map {
            it.getOrNull(0)
        }
    private val ocrQueueJobWorkInfoFlow =
        workManager.getWorkInfosForUniqueWorkFlow(OcrQueueJob.WORK_NAME).map {
            it.getOrNull(0)
        }

    private val workerProgress =
        enqueueCheckerJobWorkInfoFlow
            .mapLatest { Progress.fromWorkInfo(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    init {
        viewModelScope.launch {
            enqueueCheckerJobWorkInfoFlow
                .map { workInfo -> workInfo?.state }
                .distinctUntilChanged()
                .collectLatest { state ->
                    if (state == WorkInfo.State.FAILED) {
                        _events.send(
                            UiEvent.StringRes(
                                R.string.ocr_queue_event_enqueue_error,
                                listOf("WorkInfo.State.FAILED"),
                            ),
                        )
                    }
                }
        }
    }

    private val bufferItemsCount =
        combine(
            bufferRepo.countChecked(),
            bufferRepo.count(),
        ) { checked, total ->
            if (total == 0) {
                null
            } else {
                BufferItemsCount(checked = checked, total = total)
            }
        }

    val uiState =
        combine(
            enqueueCheckerJobWorkInfoFlow,
            ocrQueueJobWorkInfoFlow,
            workerProgress,
            bufferItemsCount,
        ) { workInfo, ocrQueueJobWorkInfo, workerProgress, bufferItemsCount ->
            UiState(
                isEnqueueCheckerRunning = workInfo?.state == WorkInfo.State.RUNNING,
                isOcrQueueRunning = ocrQueueJobWorkInfo?.state == WorkInfo.State.RUNNING,
                workerProgress = workerProgress,
                bufferItemsCount = bufferItemsCount,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(2.seconds.inWholeMilliseconds),
            UiState(),
        )

    fun addImageFiles(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val count = uris.size
            val batchId =
                batchRepo.insert(
                    OcrQueueEnqueueBatch(
                        insertedAt = Clock.System.now(),
                        options = ocrQueuePreferences.value.toEnqueueOptions(),
                    ),
                )
            bufferRepo.insertBatch(uris.associateWith { OcrQueueUriType.FILE }, batchId)

            requestWork()
            _events.send(
                UiEvent.PluralRes(R.plurals.ocr_queue_event_files_enqueued, count, listOf(count)),
            )
        }
    }

    fun addFolder(folder: PlatformFile) {
        viewModelScope.launch(Dispatchers.IO) {
            val batchId =
                batchRepo.insert(
                    OcrQueueEnqueueBatch(
                        insertedAt = Clock.System.now(),
                        options = ocrQueuePreferences.value.toEnqueueOptions(),
                    ),
                )
            bufferRepo.insertBatch(mapOf(folder.toAndroidUri() to OcrQueueUriType.FOLDER), batchId)

            requestWork()
            _events.send(
                UiEvent.StringRes(R.string.ocr_queue_event_folder_enqueued, listOf(folder.name)),
            )
        }
    }

    fun requestWork() {
        val workRequest =
            OneTimeWorkRequestBuilder<OcrQueueEnqueueCheckerJob>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

        workManager.enqueueUniqueWork(
            OcrQueueEnqueueCheckerJob.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            workRequest,
        )
    }

    fun cancelWork() {
        workManager.cancelUniqueWork(OcrQueueEnqueueCheckerJob.WORK_NAME)
    }

    fun clearBuffer() {
        viewModelScope.launch {
            bufferRepo.deleteAll()
            _events.send(UiEvent.StringRes(R.string.ocr_queue_event_enqueue_buffer_cleared))
        }
    }
}

private fun OcrQueuePreferences.toEnqueueOptions() =
    OcrQueueEnqueueOptions(
        checkIsImage = checkIsImage,
        checkIsArcaeaImage = checkIsArcaeaImage,
    )
