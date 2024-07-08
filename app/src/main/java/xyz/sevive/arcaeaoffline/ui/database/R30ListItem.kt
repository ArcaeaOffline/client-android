package xyz.sevive.arcaeaoffline.ui.database

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
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.withStyle
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultCard

@Composable
internal fun DatabaseR30ListItem(
    uiItem: DatabaseR30ListViewModel.UiItem,
    modifier: Modifier = Modifier,
) {
    val indexTextStyle = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
    val indexText = remember(uiItem.index) {
        buildAnnotatedString {
            append("#")
            withStyle(indexTextStyle.toSpanStyle()) { append("${uiItem.index + 1}") }
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
            playResult = uiItem.playResult,
            Modifier.weight(1f),
            chart = uiItem.chart,
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
            Text(uiItem.potentialText, style = MaterialTheme.typography.labelMedium)
        }
    }
}


