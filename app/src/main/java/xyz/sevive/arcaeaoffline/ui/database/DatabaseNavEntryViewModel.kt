package xyz.sevive.arcaeaoffline.ui.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.core.database.entities.Property
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

class DatabaseNavEntryViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    val propertyVersion: StateFlow<Property?> =
        repositoryContainer.propertyRepository.find("version").stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
            initialValue = null
        )

    val databaseInitialized = propertyVersion.map { it?.value != null }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = false,
    )

    val packList = repositoryContainer.packRepo.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf()
    )

    val songList = repositoryContainer.songRepo.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf()
    )

    val difficultyList = repositoryContainer.difficultyRepo.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf()
    )

    val chartInfoList = repositoryContainer.chartInfoRepo.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf()
    )

    val scoreList = repositoryContainer.playResultRepo.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf()
    )

    suspend fun initDatabase() {
        repositoryContainer.propertyRepository.upsert(Property("version", "4"))
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}
