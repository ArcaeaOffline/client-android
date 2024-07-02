package xyz.sevive.arcaeaoffline.ui.activities

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.sevive.arcaeaoffline.EmergencyModeActivity


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

    fun startEmergencyMode(context: Context) {
        context.startActivity(Intent(context, EmergencyModeActivity::class.java))
    }
}
