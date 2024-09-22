package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.enqueuechecker

import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesSerializer
import xyz.sevive.arcaeaoffline.helpers.OcrQueueEnqueueCheckerJob
import xyz.sevive.arcaeaoffline.ui.containers.OcrQueueDatabaseRepositoryContainer
import kotlin.time.Duration.Companion.seconds


class OcrQueueEnqueueCheckerViewModel(
    private val workManager: WorkManager,
    ocrQueueRepos: OcrQueueDatabaseRepositoryContainer,
    preferencesRepository: OcrQueuePreferencesRepository,
) : ViewModel() {
    private val bufferRepo = ocrQueueRepos.ocrQueueEnqueueBufferRepo

    private val ocrQueuePreferences = preferencesRepository.preferencesFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        OcrQueuePreferencesSerializer.defaultValue,
    )

    data class UiState(
        val isPreparing: Boolean = false,
        val isRunning: Boolean = false,
        val progress: Pair<Int, Int>? = null,
    )

    private val jobWorkInfo =
        workManager.getWorkInfosForUniqueWorkFlow(OcrQueueEnqueueCheckerJob.WORK_NAME).map {
            it.getOrNull(0)
        }

    private val isPreparing = MutableStateFlow(false)

    private val progress = combine(
        jobWorkInfo,
        bufferRepo.countChecked(),
        bufferRepo.count(),
    ) { workInfo, p, t ->
        if (workInfo?.state == WorkInfo.State.RUNNING && t > 0) Pair(p, t)
        else if (workInfo?.state == WorkInfo.State.ENQUEUED) Pair(0, -1)
        else null
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), null)

    val uiState = combine(jobWorkInfo, isPreparing, progress) { workInfo, isPreparing, progress ->
        UiState(
            isPreparing = isPreparing,
            isRunning = workInfo?.state == WorkInfo.State.RUNNING,
            progress = progress
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(2.seconds.inWholeMilliseconds),
        UiState()
    )

    fun addImageFiles(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            bufferRepo.insertBatch(uris)

            val workRequest = OneTimeWorkRequestBuilder<OcrQueueEnqueueCheckerJob>().setInputData(
                Data.Builder().putBoolean(
                    OcrQueueEnqueueCheckerJob.KEY_INPUT_CHECK_IS_IMAGE,
                    ocrQueuePreferences.value.checkIsImage
                ).putBoolean(
                    OcrQueueEnqueueCheckerJob.KEY_INPUT_CHECK_IS_ARCAEA_IMAGE,
                    ocrQueuePreferences.value.checkIsArcaeaImage
                ).putInt(
                    OcrQueueEnqueueCheckerJob.KEY_PARALLEL_COUNT,
                    ocrQueuePreferences.value.parallelCount
                ).build()
            ).setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)

            workManager.enqueueUniqueWork(
                OcrQueueEnqueueCheckerJob.WORK_NAME, ExistingWorkPolicy.REPLACE, workRequest.build()
            )
        }
    }

    fun addFolder(folder: DocumentFile) {
        viewModelScope.launch(Dispatchers.IO) {
            isPreparing.value = true
            try {
                val uris = folder.listFiles().filter { it.isFile }.map { it.uri }
                addImageFiles(uris)
            } finally {
                isPreparing.value = false
            }
        }
    }

    fun cancelWork() {
        workManager.cancelUniqueWork(OcrQueueEnqueueCheckerJob.WORK_NAME)
    }
}
