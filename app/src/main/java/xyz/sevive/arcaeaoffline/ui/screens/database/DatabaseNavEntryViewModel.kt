package xyz.sevive.arcaeaoffline.ui.screens.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
            propertyRepo.setScoringMode(mode)
        }
    }
}
