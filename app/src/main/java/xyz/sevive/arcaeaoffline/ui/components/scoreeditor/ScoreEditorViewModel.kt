package xyz.sevive.arcaeaoffline.ui.components.scoreeditor

import androidx.lifecycle.ViewModel
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreModifier
import xyz.sevive.arcaeaoffline.core.database.entities.Score

class ScoreEditorViewModel : ViewModel() {
    fun longToLocalDateTime(date: Long?): LocalDateTime? {
        return if (date != null) LocalDateTime.ofEpochSecond(date, 0, ZoneOffset.UTC) else null
    }

    fun editScore(_score: Score, score: Int): Score {
        return _score.copy(score = score)
    }

    fun editPure(score: Score, pure: Int?): Score {
        return score.copy(pure = pure)
    }

    fun editFar(score: Score, far: Int?): Score {
        return score.copy(far = far)
    }

    fun editLost(score: Score, lost: Int?): Score {
        return score.copy(lost = lost)
    }

    fun editDate(score: Score, date: LocalDateTime?): Score {
        return score.copy(date = date?.toEpochSecond(ZoneOffset.UTC))
    }

    fun editMaxRecall(score: Score, maxRecall: Int?): Score {
        return score.copy(maxRecall = maxRecall)
    }

    fun editModifier(score: Score, modifier: ArcaeaScoreModifier?): Score {
        return score.copy(modifier = modifier)
    }

    fun editClearType(score: Score, clearType: ArcaeaScoreClearType?): Score {
        return score.copy(clearType = clearType)
    }

    fun editComment(score: Score, comment: String?): Score {
        return score.copy(comment = comment)
    }
}
