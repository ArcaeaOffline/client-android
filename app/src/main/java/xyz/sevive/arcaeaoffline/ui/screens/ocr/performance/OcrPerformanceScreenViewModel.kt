package xyz.sevive.arcaeaoffline.ui.screens.ocr.performance

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.core.Mat
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import xyz.sevive.arcaeaoffline.core.ocr.device.OcrPerformanceBenchmark
import xyz.sevive.arcaeaoffline.core.ocr.opencv.use
import xyz.sevive.arcaeaoffline.datastore.OcrQueueParallelism
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesRepository
import java.io.IOException
import kotlin.math.round
import kotlin.time.Clock
import kotlin.uuid.Uuid

class OcrPerformanceScreenViewModel(
    context: Context,
    private val preferencesRepository: OcrQueuePreferencesRepository,
) : ViewModel() {
    companion object {
        private const val LOG_TAG = "OcrPerfScreenVM"

        private val parallelCountIntRange = OcrQueueParallelism.countRange()
        val parallelCountSliderRange = parallelCountIntRange.first.toFloat()..parallelCountIntRange.last.toFloat()
        val parallelCountSliderSteps = parallelCountIntRange.count() - 1
    }

    data class HistoryEntry(
        val uuid: Uuid = Uuid.generateV4(),
        val timestamp: kotlin.time.Instant,
        val parallel: Int,
        val result: OcrPerformanceBenchmark.Result,
    )

    data class UiState(
        val selectedImageUris: List<Uri> = emptyList(),
        val imageLoadError: Boolean = false,
        val parallelCount: Int = OcrQueueParallelism.defaultCount(),
        val parallelCountInitialized: Boolean = false,
        val running: Boolean = false,
        val runningParallel: Int? = null,
        val progress: Int = 0,
        val progressTotal: Int = 0,
        val result: OcrPerformanceBenchmark.Result? = null,
        val resultParallel: Int? = null,
        val history: List<HistoryEntry> = emptyList(),
        val errorMessage: String? = null,
    )

    private val applicationContext = context.applicationContext
    private val logger = Logger.withTag(LOG_TAG)
    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    private var benchmarkJob: Job? = null

    init {
        viewModelScope.launch {
            preferencesRepository.preferencesFlow.collectLatest { preferences ->
                _uiState.update {
                    // Initial slider value follows the production config
                    if (it.parallelCountInitialized) {
                        it
                    } else {
                        it.copy(parallelCount = preferences.parallelCount, parallelCountInitialized = true)
                    }
                }
            }
        }
    }

    fun onImagesPicked(uris: List<Uri>) {
        _uiState.update { it.copy(selectedImageUris = uris, imageLoadError = false, errorMessage = null) }
    }

    fun clearImages() {
        _uiState.update { it.copy(selectedImageUris = emptyList(), imageLoadError = false) }
    }

    fun onParallelCountChange(value: Float) {
        _uiState.update { it.copy(parallelCount = round(value).toInt().coerceIn(parallelCountIntRange)) }
    }

    fun runBenchmark() {
        val state = _uiState.value
        if (state.running) return

        benchmarkJob =
            viewModelScope.launch {
                val parallel = state.parallelCount
                _uiState.update {
                    it.copy(
                        running = true,
                        runningParallel = parallel,
                        progress = 0,
                        progressTotal = 0,
                        result = null,
                        resultParallel = null,
                        errorMessage = null,
                    )
                }
                try {
                    // no suspension point between decode and run, so ownership
                    // of the ROI Mats transfers to runBenchmark without a
                    // cancellation window
                    val result =
                        withContext(Dispatchers.Default) {
                            OcrPerformanceBenchmark.runBenchmark(
                                context = applicationContext,
                                roiSets = decodeAndExtractRois(state.selectedImageUris),
                                parallel = parallel,
                            ) { completed, total ->
                                _uiState.update { it.copy(progress = completed, progressTotal = total) }
                            }
                        }
                    _uiState.update {
                        it.copy(
                            result = result,
                            resultParallel = parallel,
                            history =
                                it.history +
                                    HistoryEntry(
                                        timestamp = Clock.System.now(),
                                        parallel = parallel,
                                        result = result,
                                    ),
                        )
                    }
                } catch (e: CancellationException) {
                    throw e
                } catch (e: ImageLoadException) {
                    logger.e(e) { "Failed to decode benchmark images" }
                    _uiState.update {
                        it.copy(
                            selectedImageUris = emptyList(),
                            imageLoadError = true,
                        )
                    }
                } catch (e: Exception) {
                    logger.e(e) { "Benchmark failed" }
                    _uiState.update { it.copy(errorMessage = e.message) }
                } finally {
                    _uiState.update { it.copy(running = false, runningParallel = null) }
                }
            }
    }

    fun cancelBenchmark() {
        benchmarkJob?.cancel()
    }

    private class ImageLoadException : IOException()

    /**
     * Decodes each image and extracts its OCR ROIs; only the ROI clones
     * survive this pass. Already-extracted ROIs are released on failure.
     */
    private suspend fun decodeAndExtractRois(uris: List<Uri>): List<List<Mat>> =
        withContext(Dispatchers.IO) {
            val roiSets = mutableListOf<List<Mat>>()
            try {
                uris.forEach { uri ->
                    // decoding below is all blocking calls, so cancellation
                    // must be polled manually between images
                    coroutineContext.ensureActive()
                    val bytes =
                        applicationContext.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                            ?: throw ImageLoadException()
                    MatOfByte(*bytes).use { matOfBytes ->
                        Imgcodecs.imdecode(matOfBytes, Imgcodecs.IMREAD_COLOR).use { img ->
                            if (img.empty()) throw ImageLoadException()
                            roiSets.add(OcrPerformanceBenchmark.extractRois(img))
                        }
                    }
                }
                roiSets
            } catch (e: Exception) {
                roiSets.flatten().forEach { it.release() }
                throw e
            }
        }
}
