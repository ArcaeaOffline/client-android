package xyz.sevive.arcaeaoffline.ui.models

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import xyz.sevive.arcaeaoffline.database.entities.Score

class OcrFromShareViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(OcrFromShareState())
    val uiState: StateFlow<OcrFromShareState> = _uiState.asStateFlow()

    fun setResult(score: Score?, error: Exception? = null) {
        _uiState.value = OcrFromShareState(score, error)
    }
}