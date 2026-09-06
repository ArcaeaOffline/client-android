package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ImageNotSupported
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.combine
import org.koin.compose.koinInject
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClassDisplay
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyWithSong
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyWithSongRepository
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.ratingClassColor

// Display data resolved as one atomic unit. Swapping the pair together is
// what keeps the card consistent (title/level text and constant change in
// the same frame) and lets the card's AnimatedContent keep its old -> new
// transition: the previous pair is held while the next one is resolving,
// instead of dropping the card to its placeholder.
data class DifficultyWithSongDisplay(
    val difficultyWithSong: DifficultyWithSong,
    val chartInfo: ChartInfo?,
)

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
    var expanded by rememberSaveable { mutableStateOf(false) }

    val jacketSize by animateDpAsState(
        targetValue = if (expanded) 50.dp else 30.dp,
        label = "jacketSize",
    )

    Card(
        onClick = { expanded = !expanded },
        modifier = modifier,
        shape = shape,
    ) {
        Row(
            Modifier.padding(dimensionResource(R.dimen.card_padding)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                Icons.Default.ImageNotSupported,
                contentDescription = null,
                modifier = Modifier.size(jacketSize),
            )

            Column(Modifier.weight(1f)) {
                Text(difficultyWithSong.title, style = MaterialTheme.typography.titleMedium)

                AnimatedVisibility(visible = expanded) {
                    Text(difficultyWithSong.artist)
                }

                // The chart info rides along in the target state: each
                // transition side renders with its own constant, otherwise the
                // outgoing text would instantly pick up the new constant.
                AnimatedContent(
                    targetState = difficultyWithSong to chartInfo,
                    transitionSpec = {
                        if (targetState.first.ratingClass > initialState.first.ratingClass) {
                            slideInVertically { height -> height } togetherWith
                                slideOutVertically { height -> -height }
                        } else {
                            slideInVertically { height -> -height } togetherWith
                                slideOutVertically { height -> height }
                        }
                    },
                    label = "ratingClassFlipping",
                ) { (dws, info) ->
                    Text(
                        text = ArcaeaFormatters.ratingText(dws, info?.constant ?: 0),
                        modifier = Modifier.fillMaxWidth(),
                        color =
                            ratingClassColor(
                                ArcaeaRatingClassDisplay.of(dws.ratingClass, dws.ratingClassAlias),
                            ),
                    )
                }
            }

            AnimatedContent(targetState = expanded, label = "unfoldIcon") {
                Icon(
                    if (it) Icons.Default.UnfoldLess else Icons.Default.UnfoldMore,
                    contentDescription = null,
                )
            }
        }
    }
}

@Preview
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
