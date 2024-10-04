package xyz.sevive.arcaeaoffline.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.datastore.AppPreferencesRepository

class SettingsViewModel(
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {
    data class AppPreferencesUiState(
        val autoSendCrashReports: Boolean = false,
    )

    val appPreferencesUiState = appPreferencesRepository.preferencesFlow.map {
        AppPreferencesUiState(
            autoSendCrashReports = it.autoSendCrashReports
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(2500L), AppPreferencesUiState())

    fun setAutoSendCrashReports(value: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            appPreferencesRepository.setAutoSendCrashReports(value)
        }
    }
}
