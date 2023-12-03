package xyz.sevive.arcaeaoffline.ui.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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

    val packList = repositoryContainer.packRepository.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf()
    )

    val songList = repositoryContainer.songRepository.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf()
    )

    val difficultyList = repositoryContainer.difficultyRepository.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf()
    )

    val chartInfoList = repositoryContainer.chartInfoRepository.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
        initialValue = listOf()
    )

    val scoreList = repositoryContainer.chartInfoRepository.findAll().stateIn(
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
