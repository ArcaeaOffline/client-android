package xyz.sevive.arcaeaoffline.ui.screens.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository

class DatabaseNavEntryViewModel(
    private val propertyRepo: PropertyRepository,
    playResultRepo: PlayResultRepository,
) : ViewModel() {
    private val logger = Logger.withTag(LOG_TAG)

    val playResultCount: StateFlow<Int> =
        playResultRepo
            .count()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                0,
            )

    companion object {
        private const val TIMEOUT_MILLIS = 1000L
        private const val LOG_TAG = "DatabaseNavEntryVM"
    }

    val scoringMode: StateFlow<ArcaeaScoringMode> =
        propertyRepo
            .scoringMode()
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                ArcaeaScoringMode.B50,
            )

    fun setScoringMode(mode: ArcaeaScoringMode) {
        viewModelScope.launch {
            runCatching { propertyRepo.setScoringMode(mode) }
                .onFailure { logger.e(it) { "Failed to set scoring mode" } }
        }
    }
}
