package xyz.sevive.arcaeaoffline.ui.components

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

class PlayResultEditorViewModel : ViewModel() {
    private val _playResult = MutableStateFlow<PlayResult?>(null)
    val playResult = _playResult.asStateFlow()

    fun setPlayResult(playResult: PlayResult) {
        _playResult.value = playResult
    }

    fun editScore(score: Int) {
        _playResult.value = playResult.value?.copy(score = score)
    }

    fun editPure(pure: Int?) {
        _playResult.value = playResult.value?.copy(pure = pure)
    }

    fun editFar(far: Int?) {
        _playResult.value = playResult.value?.copy(far = far)
    }

    fun editLost(lost: Int?) {
        _playResult.value = playResult.value?.copy(lost = lost)
    }

    fun editDate(date: Instant?) {
        _playResult.value = playResult.value?.copy(date = date)
    }

    fun editMaxRecall(maxRecall: Int?) {
        _playResult.value = playResult.value?.copy(maxRecall = maxRecall)
    }

    fun editModifier(modifier: ArcaeaPlayResultModifier?) {
        _playResult.value = playResult.value?.copy(modifier = modifier)
    }

    fun editClearType(clearType: ArcaeaPlayResultClearType?) {
        _playResult.value = playResult.value?.copy(clearType = clearType)
    }

    fun editComment(comment: String?) {
        _playResult.value = playResult.value?.copy(comment = comment)
    }
}
