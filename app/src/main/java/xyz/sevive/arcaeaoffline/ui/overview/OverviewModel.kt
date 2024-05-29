package xyz.sevive.arcaeaoffline.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer

class OverviewModel(
    arcaeaOfflineDatabaseRepositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer
) : ViewModel() {
    val b30 = arcaeaOfflineDatabaseRepositoryContainer.calculatedPotentialRepo.b30().stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(1000),
        initialValue = null,
    )
}
