package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue.preferences

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesRepository
import kotlin.time.Duration.Companion.seconds


class OcrQueuePreferencesViewModel(
    private val preferencesRepository: OcrQueuePreferencesRepository,
) : ViewModel() {
    data class PreferencesUiState(
        val checkIsImage: Boolean = false,
        val checkIsArcaeaImage: Boolean = false,
        val parallelCount: Int = -1,

        val parallelCountMin: Int = 1,
        val parallelCountMax: Int = Runtime.getRuntime().availableProcessors() * 2,
    ) {
        val parallelCountSliderRange = parallelCountMin.toFloat()..parallelCountMax.toFloat()
        val parallelCountSliderSteps = parallelCountMax - parallelCountMin - 1
    }

    val preferencesUiState = preferencesRepository.preferencesFlow.map {
        PreferencesUiState(
            checkIsImage = it.checkIsImage,
            checkIsArcaeaImage = it.checkIsArcaeaImage,
            parallelCount = it.parallelCount,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(1.seconds.inWholeMilliseconds),
        PreferencesUiState(),
    )

    fun setCheckIsImage(value: Boolean) {
        viewModelScope.launch { preferencesRepository.setCheckIsImage(value) }
    }

    fun setCheckIsArcaeaImage(value: Boolean) {
        viewModelScope.launch { preferencesRepository.setCheckIsArcaeaImage(value) }
    }

    fun setParallelCount(value: Int) {
        viewModelScope.launch { preferencesRepository.setParallelCount(value) }
    }
}
