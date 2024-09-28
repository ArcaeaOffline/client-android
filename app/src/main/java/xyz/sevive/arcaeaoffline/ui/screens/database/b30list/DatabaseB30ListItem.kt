package xyz.sevive.arcaeaoffline.ui.screens.database.b30list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import com.jakewharton.threetenabp.AndroidThreeTen
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResultBest
import xyz.sevive.arcaeaoffline.core.database.entities.toPlayResult
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultCard
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import java.util.UUID

@Composable
internal fun DatabaseB30ListItem(
    item: DatabaseB30ListViewModel.ListItem,
    modifier: Modifier = Modifier,
) {
    val indexTextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    val indexText = remember(item.index) {
        buildAnnotatedString {
            append("#")
            withStyle(indexTextStyle.toSpanStyle()) { append("${item.index + 1}") }
        }
    }

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val minTextWidth = remember {
        density.run { textMeasurer.measure("#00", style = indexTextStyle).size.width.toDp() }
    }

    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
        verticalAlignment = Alignment.Bottom
    ) {
        ArcaeaPlayResultCard(
            playResult = item.playResultBest.toPlayResult(),
            Modifier.weight(1f),
            chart = item.chart,
        )

        Column(
            Modifier
                .defaultMinSize(minWidth = minTextWidth)
                .height(IntrinsicSize.Max),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(indexText)

            Spacer(Modifier.height(dimensionResource(R.dimen.list_padding)))

            Text("PTT", style = MaterialTheme.typography.labelSmall)
            Text(item.potentialText, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Preview
@Composable
private fun DatabaseB30ListItemPreview() {
    AndroidThreeTen.init(LocalContext.current)

    fun fakeListItem(index: Int, withChart: Boolean = true): DatabaseB30ListViewModel.ListItem {
        return DatabaseB30ListViewModel.ListItem(
            index = index,
            playResultBest = PlayResultBest(
                id = 1,
                uuid = UUID.randomUUID(),
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                score = 99500000,
                pure = null,
                shinyPure = null,
                far = null,
                lost = null,
                date = Instant.ofEpochMilli(0),
                maxRecall = null,
                modifier = null,
                clearType = null,
                potential = 12.00,
                comment = null,
            ),
            chart = if (withChart) Chart(
                songIdx = 0,
                songId = "test",
                ratingClass = ArcaeaRatingClass.FUTURE,
                rating = 9,
                ratingPlus = true,
                title = "Preview",
                artist = "Preview",
                set = "preview",
                audioOverride = false,
                jacketOverride = false,
                constant = 90,
                side = 0,
            ) else null,
        )
    }

    ArcaeaOfflineTheme {
        Surface {
            Column {
                DatabaseB30ListItem(fakeListItem(index = 0))
                DatabaseB30ListItem(fakeListItem(index = 1, withChart = true))
            }
        }
    }
}

