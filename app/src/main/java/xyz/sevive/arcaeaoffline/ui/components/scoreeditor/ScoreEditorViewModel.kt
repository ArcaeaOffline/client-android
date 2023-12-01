package xyz.sevive.arcaeaoffline.ui.components.scoreeditor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreClearType
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreModifier
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Score

class ScoreEditorViewModel : ViewModel() {
    private val _songId = MutableStateFlow<String?>(null)
    val songId = _songId.asStateFlow()

    private val _ratingClass = MutableStateFlow<ArcaeaScoreRatingClass?>(null)
    val ratingClass = _ratingClass.asStateFlow()

    fun setChart(chart: Chart) {
        _songId.value = chart.songId
        _ratingClass.value = ArcaeaScoreRatingClass.fromInt(chart.ratingClass)
    }

    fun setChart(songId: String, ratingClass: Int) {
        _songId.value = songId
        _ratingClass.value = ArcaeaScoreRatingClass.fromInt(ratingClass)
    }

    private val _score = MutableStateFlow(0)
    val score = _score.asStateFlow()

    fun setScore(value: Int) {
        _score.value = value
    }

    private fun <T> nullableStateFlow(
        originalFlow: MutableStateFlow<T>, isNullFlow: MutableStateFlow<Boolean>
    ): StateFlow<T?> {
        return originalFlow.combine(isNullFlow) { value, isNull -> if (isNull) null else value }
            .stateIn(viewModelScope, SharingStarted.Eagerly, null)
    }

    private val _pure = MutableStateFlow(0)
    private val _pureIsNull = MutableStateFlow(false)
    val pure = nullableStateFlow(_pure, _pureIsNull)

    private val _far = MutableStateFlow(0)
    private val _farIsNull = MutableStateFlow(false)
    val far = nullableStateFlow(_far, _farIsNull)

    private val _lost = MutableStateFlow(0)
    private val _lostIsNull = MutableStateFlow(false)
    val lost = nullableStateFlow(_lost, _lostIsNull)

    private val _maxRecall = MutableStateFlow(0)
    private val _maxRecallIsNull = MutableStateFlow(true)
    val maxRecall = nullableStateFlow(_maxRecall, _maxRecallIsNull)

    fun setPure(value: Int) {
        _pure.value = value
    }

    fun setPureIsNull(value: Boolean) {
        _pureIsNull.value = value
    }

    fun setFar(value: Int) {
        _far.value = value
    }

    fun setFarIsNull(value: Boolean) {
        _farIsNull.value = value
    }

    fun setLost(value: Int) {
        _lost.value = value
    }

    fun setLostIsNull(value: Boolean) {
        _lostIsNull.value = value
    }

    fun setMaxRecall(value: Int) {
        _maxRecall.value = value
    }

    fun setMaxRecallIsNull(value: Boolean) {
        _maxRecallIsNull.value = value
    }

    private val _date = MutableStateFlow<LocalDateTime>(LocalDateTime.now())
    private val _dateIsNull = MutableStateFlow(false)
    val date = nullableStateFlow(_date, _dateIsNull)

    fun setDate(value: LocalDateTime) {
        _date.value = value
    }

    fun setDateIsNull(value: Boolean) {
        _dateIsNull.value = value
    }

    private val _clearType = MutableStateFlow(ArcaeaScoreClearType.NORMAL_CLEAR)
    private val _clearTypeIsNull = MutableStateFlow(true)
    val clearType = nullableStateFlow(_clearType, _clearTypeIsNull)

    private val _modifier = MutableStateFlow(ArcaeaScoreModifier.NORMAL)
    private val _modifierIsNull = MutableStateFlow(true)
    val modifier = nullableStateFlow(_modifier, _modifierIsNull)

    fun setClearType(value: ArcaeaScoreClearType) {
        _clearType.value = value
    }

    fun setClearType(value: Int) {
        setClearType(ArcaeaScoreClearType.fromInt(value))
    }

    fun setClearTypeIsNull(value: Boolean) {
        _clearTypeIsNull.value = value
    }

    fun setModifier(value: ArcaeaScoreModifier) {
        _modifier.value = value
    }

    fun setModifier(value: Int) {
        setModifier(ArcaeaScoreModifier.fromInt(value))
    }

    fun setModifierIsNull(value: Boolean) {
        _modifierIsNull.value = value
    }

    private val _comment = MutableStateFlow("")
    private val _commentIsNull = MutableStateFlow(true)
    val comment = nullableStateFlow(_comment, _commentIsNull)

    fun setComment(value: String) {
        _comment.value = value
    }

    fun setCommentIsNull(value: Boolean) {
        _commentIsNull.value = value
    }

    private fun <T> setNullableFlowFromValue(
        originalFlow: MutableStateFlow<T>, isNullFlow: MutableStateFlow<Boolean>, value: T?
    ) {
        if (value != null) {
            originalFlow.value = value
            isNullFlow.value = false
        } else {
            isNullFlow.value = true
        }
    }

    fun setArcaeaScore(score: Score) {
        _songId.value = score.songId
        _ratingClass.value = ArcaeaScoreRatingClass.fromInt(score.ratingClass)
        _score.value = score.score
        setNullableFlowFromValue(_pure, _pureIsNull, score.pure)
        setNullableFlowFromValue(_far, _farIsNull, score.far)
        setNullableFlowFromValue(_lost, _lostIsNull, score.lost)

        setNullableFlowFromValue(
            _date, _dateIsNull,
            if (score.date != null) {
                Instant.ofEpochSecond(
                    score.date.toLong()
                ).atZone(ZoneId.systemDefault()).toLocalDateTime()
            } else null,
        )

        setNullableFlowFromValue(_maxRecall, _maxRecallIsNull, score.maxRecall)
        setNullableFlowFromValue(
            _clearType, _clearTypeIsNull,
            if (score.clearType != null) {
                ArcaeaScoreClearType.fromInt(score.clearType)
            } else null,
        )
        setNullableFlowFromValue(
            _modifier, _modifierIsNull,
            if (score.modifier != null) {
                ArcaeaScoreModifier.fromInt(score.modifier)
            } else null,
        )
        setNullableFlowFromValue(_comment, _commentIsNull, score.comment)
    }

    fun toArcaeaScore(): Score {
        if (songId.value == null) {
            throw IllegalArgumentException("songId not set")
        }

        if (ratingClass.value == null) {
            throw IllegalArgumentException("ratingClass not set")
        }

        return Score(
            id = 0,
            songId = songId.value!!,
            ratingClass = ratingClass.value!!.value,
            score = score.value,
            pure = pure.value,
            far = far.value,
            lost = lost.value,
            date = date.value?.toEpochSecond(ZoneOffset.UTC),
            maxRecall = maxRecall.value,
            clearType = clearType.value?.value,
            modifier = modifier.value?.value,
            comment = comment.value,
        )
    }

    val arcaeaScoreFlow: Flow<Score?> = combine(
        songId, ratingClass, score, pure, far, lost, maxRecall, date, clearType, modifier, comment
    ) {
        try {
            toArcaeaScore()
        } catch (e: Exception) {
            null
        }
    }
}
