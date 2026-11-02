package xyz.sevive.arcaeaoffline.ui.screens.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.repositories.MetaRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository

class DatabaseNavEntryViewModel(
    metaRepo: MetaRepository,
    private val propertyRepo: PropertyRepository,
    playResultRepo: PlayResultRepository,
) : ViewModel() {
    class StatusUiState(
        val databaseVersion: Int = 0,
        val databaseSchemaVersion: Int = 0,
        val playResultCount: Int = 0,
    )

    val statusUiState =
        combine(
            propertyRepo.databaseVersion(),
            playResultRepo.count(),
            // TODO: move songRepo.countDeletedInGame(),
        ) { versionInfo, playResultCount ->
            StatusUiState(
                databaseVersion = versionInfo ?: 0,
                databaseSchemaVersion = metaRepo.schemaVersion(),
                playResultCount = playResultCount,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            StatusUiState(),
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
