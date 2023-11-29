package xyz.sevive.arcaeaoffline.ui.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

class DatabaseScoreListViewModel(
    repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    val scoreList = repositoryContainer.scoreRepository.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(),
    )
}
