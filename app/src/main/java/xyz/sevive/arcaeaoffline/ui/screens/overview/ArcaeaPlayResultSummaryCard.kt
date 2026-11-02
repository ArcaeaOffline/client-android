package xyz.sevive.arcaeaoffline.ui.screens.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClassDisplay
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.playRating
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedDateTime
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha
import xyz.sevive.arcaeaoffline.ui.components.rememberArcaeaChartDisplay
import xyz.sevive.arcaeaoffline.ui.components.rememberRatingClassAccent
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.theme.playResultGradeGradientBrush
import xyz.sevive.arcaeaoffline.ui.theme.spacing

/**
 * A single play result at a glance: chart, grade, score, play rating and date.
 *
 * Falls back to the raw song id and rating class when the chart is not in the database.
 */
@Composable
internal fun ArcaeaPlayResultSummaryCard(
    playResult: PlayResult,
    scoringMode: ArcaeaScoringMode,
    modifier: Modifier = Modifier,
) {
    val resources = LocalResources.current
    val display by rememberArcaeaChartDisplay(playResult.songId, playResult.ratingClass)

    val title = display?.difficultyWithSong?.title ?: playResult.songId
    val ratingClassDisplay =
        display?.difficultyWithSong?.let { ArcaeaRatingClassDisplay.of(it.ratingClass, it.ratingClassAlias) }
            ?: ArcaeaRatingClassDisplay.of(playResult.ratingClass)
    val levelText =
        display?.let { ArcaeaFormatters.ratingTextParts(it.difficultyWithSong, it.chartInfo?.constant ?: 0).value }
    val difficultyText = listOfNotNull(ratingClassDisplay.name, levelText).joinToString(" ")
    val accent = rememberRatingClassAccent(ratingClassDisplay)

    val gradeText = remember(playResult.score) { ArcaeaFormatters.scoreToLevelText(playResult.score) }
    val scoreText = remember(playResult.score) { ArcaeaFormatters.score(playResult.score) }
    val ratingText =
        remember(display, playResult, scoringMode) {
            ArcaeaFormatters.potentialToText(display?.chartInfo?.let { playResult.playRating(it, scoringMode) })
        }
    val dateText =
        remember(playResult.date) {
            playResult.date?.formatAsLocalizedDateTime() ?: resources.getString(R.string.play_result_no_date)
        }

    Card(modifier) {
        Column(
            Modifier
                .background(accent.backgroundBrush)
                .padding(horizontal = MaterialTheme.spacing.pagePadding, vertical = MaterialTheme.spacing.cardPadding),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )

                Spacer(Modifier.width(MaterialTheme.spacing.sm))

                Text(
                    difficultyText,
                    style = MaterialTheme.typography.labelLarge,
                    color = accent.textColor,
                )
            }

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    gradeText,
                    style =
                        MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            brush = playResultGradeGradientBrush(playResult.score),
                        ),
                )

                Text(scoreText, style = MaterialTheme.typography.titleMedium)

                Spacer(Modifier.weight(1f))

                Text("PTT", fontWeight = FontWeight.Thin, style = MaterialTheme.typography.labelSmall)

                Text(ratingText, style = MaterialTheme.typography.titleMedium)
            }

            Text(
                dateText,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Normal),
                modifier = Modifier.secondaryItemAlpha(),
            )
        }
    }
}
