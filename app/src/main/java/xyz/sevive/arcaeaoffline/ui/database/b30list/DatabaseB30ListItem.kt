package xyz.sevive.arcaeaoffline.ui.database.b30list

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.toScore
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaScoreCard
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters

@Composable
internal fun DatabaseB30ListItem(
    uiItem: DatabaseB30ListUiItem,
    modifier: Modifier = Modifier,
) {
    val (index, scoreBest, chart) = uiItem

    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
        verticalAlignment = Alignment.Bottom
    ) {
        ArcaeaScoreCard(
            score = scoreBest.toScore(), Modifier.weight(1f), chart = chart
        )

        Column(
            Modifier
                .defaultMinSize(minWidth = 40.dp)
                .height(IntrinsicSize.Max),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(buildAnnotatedString {
                append("#")
                withStyle(
                    SpanStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.titleLarge.fontSize
                    )
                ) {
                    append("${index + 1}")
                }
            })

            Spacer(Modifier.height(dimensionResource(R.dimen.list_padding)))

            Text("PTT", style = MaterialTheme.typography.labelSmall)
            Text(
                ArcaeaFormatters.potentialToText(scoreBest.potential),
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}
