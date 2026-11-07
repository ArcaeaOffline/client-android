package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearStatus
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreGrade
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.playRating
import xyz.sevive.arcaeaoffline.core.search.SearchEngine
import xyz.sevive.arcaeaoffline.core.validation.ArcaeaPlayResultValidator
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.helpers.UiDisplayChartCacheHolder

/**
 * A play result row: the play result plus the joined chart data the list renders.
 */
data class PlayResultListUiItem(
    val playResult: PlayResult,
    val display: UiDisplayChartCacheHolder.Entry? = null,
    val isDeletedInGame: Boolean = false,
    val scoringMode: ArcaeaScoringMode,
) {
    val uuid = playResult.uuid
    val playRating = display?.chartInfo?.let { playResult.playRating(it, scoringMode) }
    val potentialText =
        buildAnnotatedString {
            val baseText = ArcaeaFormatters.potentialToText(playRating)

            if (isDeletedInGame) {
                pushStyle(
                    SpanStyle(
                        textDecoration = TextDecoration.LineThrough,
                        fontWeight = FontWeight.Light,
                    ),
                )
            }
            append(baseText)
        }
    val warnings = ArcaeaPlayResultValidator.validate(playResult, display?.chartInfo, scoringMode)
    val scoreGrade = ArcaeaScoreGrade.of(playResult.score)
    val clearStatus = ArcaeaPlayResultClearStatus.of(playResult.clearType)
    val normalizedComment = playResult.comment?.let(SearchEngine::normalize)

    val title: String = display?.difficultyWithSong?.title ?: playResult.songId
}
