package xyz.sevive.arcaeaoffline.ui.screens.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyLocalizedRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.MetaRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PackLocalizedRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PackRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongLocalizedRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository

class DatabaseNavEntryViewModel(
    metaRepo: MetaRepository,
    propertyRepo: PropertyRepository,
    packRepo: PackRepository,
    songRepo: SongRepository,
    difficultyRepo: DifficultyRepository,
    chartInfoRepo: ChartInfoRepository,
    playResultRepo: PlayResultRepository,
    packLocalizedRepo: PackLocalizedRepository,
    songLocalizedRepo: SongLocalizedRepository,
    difficultyLocalizedRepo: DifficultyLocalizedRepository,
) : ViewModel() {
    class StatusUiState(
        val databaseVersion: Int = 0,
        val databaseSchemaVersion: Int = 0,
        val packCount: Int = 0,
        val songCount: Int = 0,
        val difficultyCount: Int = 0,
        val chartInfoCount: Int = 0,
        val playResultCount: Int = 0,
        val packLocalizedCount: Int = 0,
        val songLocalizedCount: Int = 0,
        val difficultyLocalizedCount: Int = 0,
        val songDeletedInGameCount: Int = 0,
    )

    val statusUiState =
        combine(
            propertyRepo.databaseVersion(),
            packRepo.count(),
            songRepo.count(),
            difficultyRepo.count(),
            chartInfoRepo.count(),
            playResultRepo.count(),
            packLocalizedRepo.count(),
            songLocalizedRepo.count(),
            difficultyLocalizedRepo.count(),
            songRepo.countDeletedInGame(),
        ) { flows ->
            StatusUiState(
                databaseVersion = flows[0] ?: 0,
                databaseSchemaVersion = metaRepo.schemaVersion(),
                packCount = flows[1] ?: 0,
                songCount = flows[2] ?: 0,
                difficultyCount = flows[3] ?: 0,
                chartInfoCount = flows[4] ?: 0,
                playResultCount = flows[5] ?: 0,
                packLocalizedCount = flows[6] ?: 0,
                songLocalizedCount = flows[7] ?: 0,
                difficultyLocalizedCount = flows[8] ?: 0,
                songDeletedInGameCount = flows[9] ?: 0,
            )
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            StatusUiState(),
        )

    companion object {
        private const val TIMEOUT_MILLIS = 1000L
    }
}
