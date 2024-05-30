package xyz.sevive.arcaeaoffline.ui.components

import androidx.lifecycle.ViewModel
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult

class PlayResultEditorViewModel : ViewModel() {
    fun editScore(playResult: PlayResult, score: Int): PlayResult {
        return playResult.copy(score = score)
    }

    fun editPure(playResult: PlayResult, pure: Int?): PlayResult {
        return playResult.copy(pure = pure)
    }

    fun editFar(playResult: PlayResult, far: Int?): PlayResult {
        return playResult.copy(far = far)
    }

    fun editLost(playResult: PlayResult, lost: Int?): PlayResult {
        return playResult.copy(lost = lost)
    }

    fun editDate(playResult: PlayResult, date: Instant?): PlayResult {
        return playResult.copy(date = date)
    }

    fun editMaxRecall(playResult: PlayResult, maxRecall: Int?): PlayResult {
        return playResult.copy(maxRecall = maxRecall)
    }

    fun editModifier(playResult: PlayResult, modifier: ArcaeaPlayResultModifier?): PlayResult {
        return playResult.copy(modifier = modifier)
    }

    fun editClearType(playResult: PlayResult, clearType: ArcaeaPlayResultClearType?): PlayResult {
        return playResult.copy(clearType = clearType)
    }

    fun editComment(playResult: PlayResult, comment: String?): PlayResult {
        return playResult.copy(comment = comment)
    }
}
