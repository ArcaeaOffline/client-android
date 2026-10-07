package xyz.sevive.arcaeaoffline.ui.screens.database.r30list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultCard
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.theme.spacing

@Composable
internal fun DatabaseR30ListItem(
    item: DatabaseR30ListViewModel.ListItem,
    modifier: Modifier = Modifier,
) {
    val indexTextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    val indexText =
        remember(item.index) {
            buildAnnotatedString {
                append("#")
                withStyle(indexTextStyle.toSpanStyle()) { append("${item.index + 1}") }
            }
        }

    val density = LocalDensity.current
    val textMeasurer = rememberTextMeasurer()
    val minTextWidth =
        remember {
            density.run {
                textMeasurer
                    .measure("#00", style = indexTextStyle)
                    .size.width
                    .toDp()
            }
        }

    val playRatingText =
        remember(item.playRating) {
            ArcaeaFormatters.potentialToText(item.playRating)
        }

    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.Bottom,
    ) {
        ArcaeaPlayResultCard(
            playResult = item.playResult,
            Modifier.weight(1f),
            difficultyWithSong = item.display?.difficultyWithSong,
            chartInfo = item.display?.chartInfo,
        )

        Column(
            Modifier
                .defaultMinSize(minWidth = minTextWidth)
                .height(IntrinsicSize.Max),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(indexText)

            Spacer(Modifier.height(MaterialTheme.spacing.sm))

            Text("PTT", style = MaterialTheme.typography.labelSmall)
            Text(playRatingText, style = MaterialTheme.typography.labelMedium)
        }
    }
}
