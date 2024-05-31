package xyz.sevive.arcaeaoffline.ui.activities

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow


class CrashReportActivityViewModel : ViewModel() {
    data class UiState(
        val comment: String? = null,
        val contact: String? = null,
        val reportProcessed: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState = _uiState.asStateFlow()

    fun setComment(comment: String?) {
        _uiState.value = uiState.value.copy(comment = comment)
    }

    fun setContact(contact: String?) {
        _uiState.value = uiState.value.copy(contact = contact)
    }

    fun reportProcessed() {
        _uiState.value = uiState.value.copy(reportProcessed = true)
    }
}
