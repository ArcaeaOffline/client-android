package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.ratingClassColor


@Composable
fun ArcaeaChartCard(
    chart: Chart,
    modifier: Modifier = Modifier,
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
            Modifier.padding(dimensionResource(R.dimen.general_card_padding)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                Icons.Default.ImageNotSupported,
                contentDescription = null,
                modifier = Modifier.size(jacketSize),
            )

            Column(Modifier.weight(1f)) {
                Text(chart.title, style = MaterialTheme.typography.titleMedium)

                AnimatedVisibility(visible = expanded) {
                    Text(chart.artist)
                }

                Text(
                    text = ArcaeaFormatters.ratingText(chart),
                    color = ratingClassColor(ArcaeaScoreRatingClass.fromInt(chart.ratingClass))
                )
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
    val chart = Chart(
        songIdx = 1,
        songId = "example",
        ratingClass = 2,
        rating = 10,
        ratingPlus = true,
        title = "Example",
        artist = "Artist",
        set = "example",
        side = 1,
        audioOverride = false,
        jacketOverride = false,
        constant = 109,
    )

    val chartLongTitle = Chart(
        songIdx = 2,
        songId = "verylong",
        ratingClass = 2,
        rating = 10,
        ratingPlus = true,
        title = "SolarOrbit -release in the Masterbranch road- Misdake -ra de et de mall-",
        artist = "Example VS Case VS Lorem VS Ipsum VS dolor VS sit VS amet feat. Preview",
        set = "example",
        side = 1,
        audioOverride = false,
        jacketOverride = false,
        constant = 109,
    )

    ArcaeaOfflineTheme {
        Column {
            ArcaeaChartCard(chart = chart, Modifier.fillMaxWidth())
            ArcaeaChartCard(chart = chartLongTitle, Modifier.fillMaxWidth())
        }
    }
}
