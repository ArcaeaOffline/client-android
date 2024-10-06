package xyz.sevive.arcaeaoffline.ui.screens.settings.unstablealert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.datastore.UnstableFlavorPreferencesRepository
import kotlin.time.Duration.Companion.seconds


class SettingsUnstableAlertScreenViewModel(
    private val prefsRepository: UnstableFlavorPreferencesRepository
) : ViewModel() {
    val unstableAlertRead = prefsRepository.preferencesFlow.map {
        it.unstableAlertRead
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(2.seconds.inWholeMilliseconds),
        null,
    )

    fun setUnstableAlertRead(value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            prefsRepository.setAlertRead(value)
        }
    }
}
