package xyz.sevive.arcaeaoffline.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.combine
import org.koin.compose.koinInject
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClassDisplay
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyWithSongRepository
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.helpers.DifficultyWithSongDisplay
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.spacing

@Composable
internal fun rememberArcaeaChartDisplay(
    songId: String?,
    ratingClass: ArcaeaRatingClass?,
): State<DifficultyWithSongDisplay?> {
    val difficultyWithSongRepo = koinInject<DifficultyWithSongRepository>()
    val chartInfoRepo = koinInject<ChartInfoRepository>()

    return produceState(initialValue = null, songId, ratingClass) {
        if (songId != null && ratingClass != null) {
            difficultyWithSongRepo
                .find(songId, ratingClass)
                .combine(chartInfoRepo.find(songId, ratingClass)) { dws, info ->
                    dws?.let { DifficultyWithSongDisplay(it, info) }
                }.collect { value = it }
        } else {
            value = null
        }
    }
}

@Composable
fun ArcaeaChartCard(
    difficultyWithSong: DifficultyWithSong,
    modifier: Modifier = Modifier,
    chartInfo: ChartInfo? = null,
    shape: Shape = CardDefaults.shape,
) {
    var expanded by rememberSaveable(difficultyWithSong) { mutableStateOf(false) }
    var titleOverflowed by remember(difficultyWithSong) { mutableStateOf(false) }
    var artistOverflowed by remember(difficultyWithSong) { mutableStateOf(false) }
    val canExpand = expanded || titleOverflowed || artistOverflowed

    val accent =
        rememberRatingClassAccent(
            ArcaeaRatingClassDisplay.of(difficultyWithSong.ratingClass, difficultyWithSong.ratingClassAlias),
        )
    val enabledCardColors = CardDefaults.cardColors()

    Card(
        onClick = { expanded = !expanded },
        modifier = modifier,
        enabled = canExpand,
        shape = shape,
        colors =
            CardDefaults.cardColors(
                disabledContainerColor = enabledCardColors.containerColor,
                disabledContentColor = enabledCardColors.contentColor,
            ),
    ) {
        Row(
            // TODO: sync this to spacing...?
            Modifier
                .background(accent.backgroundBrush)
                .padding(horizontal = MaterialTheme.spacing.pagePadding, vertical = MaterialTheme.spacing.cardPadding),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    difficultyWithSong.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { if (!expanded) titleOverflowed = it.hasVisualOverflow },
                )

                Text(
                    difficultyWithSong.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis,
                    onTextLayout = { if (!expanded) artistOverflowed = it.hasVisualOverflow },
                )
            }

            // The chart info rides along in the target state: each
            // transition side renders with its own constant, otherwise the
            // outgoing text would instantly pick up the new constant.
            AnimatedContent(
                targetState = difficultyWithSong to chartInfo,
                transitionSpec = {
                    if (targetState.first.ratingClass > initialState.first.ratingClass) {
                        slideInVertically { height -> height } togetherWith slideOutVertically { height -> -height }
                    } else {
                        slideInVertically { height -> -height } togetherWith slideOutVertically { height -> height }
                    }
                },
                label = "ratingClassFlipping",
            ) { (dws, info) ->
                val (ratingClass, value) = ArcaeaFormatters.ratingTextParts(dws, info?.constant ?: 0)

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        value,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium,
                        color = accent.textColor,
                    )
                    Text(
                        ratingClass,
                        style = MaterialTheme.typography.bodyMedium,
                        color = accent.textColor,
                    )
                }
            }

            if (canExpand) {
                AnimatedContent(targetState = expanded, label = "unfoldIcon") {
                    Icon(
                        if (it) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = accent.textColor,
                    )
                }
            }
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ArcaeaChartCardPreview() {
    val difficulty =
        DifficultyWithSong(
            songId = "example",
            ratingClass = ArcaeaRatingClass.FUTURE,
            rating = 10,
            ratingPlus = true,
            title = "Example",
            artist = "Artist",
        )

    val difficultyLongTitle =
        DifficultyWithSong(
            songId = "verylong",
            ratingClass = ArcaeaRatingClass.FUTURE,
            rating = 10,
            ratingPlus = true,
            title = "SolarOrbit -release in the Masterbranch road- Misdake -ra de et de mall-",
            artist = "Example VS Case VS Lorem VS Ipsum VS dolor VS sit VS amet feat. Preview",
        )

    ArcaeaOfflineTheme {
        Column {
            ArcaeaChartCard(
                difficulty,
                Modifier.fillMaxWidth(),
                chartInfo = ChartInfo("example", ArcaeaRatingClass.FUTURE, constant = 109, notes = null),
            )
            ArcaeaChartCard(difficultyLongTitle, Modifier.fillMaxWidth())
        }
    }
}
