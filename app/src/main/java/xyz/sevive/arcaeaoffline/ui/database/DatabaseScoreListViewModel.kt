package xyz.sevive.arcaeaoffline.ui.database

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

class DatabaseScoreListViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    val scoreList = repositoryContainer.scoreRepository.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(),
    )

    val scoreCalculatedList = repositoryContainer.scoreCalculatedRepository.findAll().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = listOf(),
    )

    suspend fun updateScore(score: Score) {
        repositoryContainer.scoreRepository.upsert(score)
    }

    suspend fun deleteScore(score: Score) {
        repositoryContainer.scoreRepository.delete(score)
    }
}
