package xyz.sevive.arcaeaoffline.ui.screens.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
private fun PotentialLabel(
    title: String,
    potential: Double?,
    titleTextStyle: TextStyle = MaterialTheme.typography.titleLarge,
    potentialTextStyle: TextStyle = MaterialTheme.typography.headlineLarge,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            title,
            style = titleTextStyle,
            fontWeight = FontWeight.Light,
        )
        Text(
            ArcaeaFormatters.potentialToText(potential),
            style = potentialTextStyle,
        )
    }
}

@Composable
internal fun OverviewPotentialCard(
    uiState: OverviewViewModel.UiState,
    modifier: Modifier = Modifier,
) {
    Card(modifier) {
        Row(Modifier.padding(dimensionResource(R.dimen.page_padding))) {
            PotentialLabel(
                title = stringResource(R.string.arcaea_potential),
                potential = uiState.potential,
                titleTextStyle = MaterialTheme.typography.headlineSmall,
                potentialTextStyle = MaterialTheme.typography.displayLarge,
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .weight(1f),
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
            ) {
                PotentialLabel(
                    title = "B30",
                    potential = uiState.b30,
                )

                PotentialLabel(
                    title = "R10",
                    potential = uiState.r10,
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun OverviewPotentialCardPreview() {
    ArcaeaOfflineTheme {
        Surface {
            OverviewPotentialCard(
                OverviewViewModel.UiState(
                    isLoading = false,
                    b30 = 13.00,
                    r10 = 13.00,
                    potential = 13.00,
                ),
                Modifier.fillMaxWidth(),
            )
        }
    }
}
