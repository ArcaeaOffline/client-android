package xyz.sevive.arcaeaoffline.ui.screens.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer


class DatabaseNavEntryViewModel(
    repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer,
    private val databaseSchemaVersion: Int,
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
    )

    val statusUiState = combine(
        repositoryContainer.propertyRepo.databaseVersion(),
        repositoryContainer.packRepo.count(),
        repositoryContainer.songRepo.count(),
        repositoryContainer.difficultyRepo.count(),
        repositoryContainer.chartInfoRepo.count(),
        repositoryContainer.playResultRepo.count(),
        repositoryContainer.packLocalizedRepo.count(),
        repositoryContainer.songLocalizedRepo.count(),
        repositoryContainer.difficultyLocalizedRepo.count(),
    ) { flows ->
        StatusUiState(
            databaseVersion = flows[0] ?: 0,
            databaseSchemaVersion = databaseSchemaVersion,
            packCount = flows[1] ?: 0,
            songCount = flows[2] ?: 0,
            difficultyCount = flows[3] ?: 0,
            chartInfoCount = flows[4] ?: 0,
            playResultCount = flows[5] ?: 0,
            packLocalizedCount = flows[6] ?: 0,
            songLocalizedCount = flows[7] ?: 0,
            difficultyLocalizedCount = flows[8] ?: 0,
        )
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(TIMEOUT_MILLIS), StatusUiState()
    )

    companion object {
        private const val TIMEOUT_MILLIS = 1000L
    }
}
