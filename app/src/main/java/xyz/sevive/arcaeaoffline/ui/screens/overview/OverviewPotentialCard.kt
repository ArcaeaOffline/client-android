package xyz.sevive.arcaeaoffline.ui.screens.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialGroup
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialGroups
import xyz.sevive.arcaeaoffline.helpers.calculatePotential
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.extendedColorScheme

@Composable
private fun PotentialRow(
    label: @Composable RowScope.() -> Unit,
    value: @Composable RowScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        label()
        value()
    }
}

@Composable
private fun PotentialRow(
    label: String,
    value: Double?,
    divideBy: Int,
    modifier: Modifier = Modifier,
) {
    val dividedValue = value?.div(divideBy)

    PotentialRow(
        label = { Text(label, Modifier.alignByBaseline()) },
        value = {
            IconRow(Modifier.alignByBaseline()) {
                Text(
                    "${ArcaeaFormatters.potentialToText(value, 4)} ÷ $divideBy",
                    Modifier.alignByBaseline(),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    Modifier.size(18.dp),
                )

                Text(
                    ArcaeaFormatters.potentialToText(dividedValue, 4),
                    Modifier.alignByBaseline(),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        },
        modifier = modifier,
    )
}

@Composable
private fun PotentialRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    labelTextStyle: TextStyle = LocalTextStyle.current,
    valueTextStyle: TextStyle = LocalTextStyle.current,
) {
    PotentialRow(
        label = { Text(label, Modifier.alignByBaseline(), style = labelTextStyle) },
        value = { Text(value, Modifier.alignByBaseline(), style = valueTextStyle) },
        modifier = modifier,
    )
}

private fun dataCompletenessWarningResIds(
    entries: PotentialGroups?,
    scoringMode: ArcaeaScoringMode,
): List<Int> =
    buildList {
        if (scoringMode == ArcaeaScoringMode.B30_R10) {
            add(R.string.potential_completeness_warning_recent_inaccuracy)
        }

        if (entries == null) return@buildList

        when (scoringMode) {
            ArcaeaScoringMode.B50 -> {
                if (!entries.b50.isComplete) {
                    add(
                        if (entries.b10.isComplete) {
                            R.string.potential_completeness_warning_b50
                        } else {
                            R.string.potential_completeness_warning_b10
                        },
                    )
                }
            }

            ArcaeaScoringMode.B30_R10 -> {
                if (!entries.b30.isComplete) add(R.string.potential_completeness_warning_b30)
                if (!entries.r10.isComplete) add(R.string.potential_completeness_warning_r10)
            }
        }
    }

@Composable
internal fun OverviewPotentialCard(
    uiState: OverviewViewModel.UiState,
    modifier: Modifier = Modifier,
) {
    val warningResIds =
        remember(uiState.entries, uiState.scoringMode) {
            dataCompletenessWarningResIds(uiState.entries, uiState.scoringMode)
        }
    val warnings = warningResIds.map { stringResource(it) }

    val mainScale =
        when (uiState.scoringMode) {
            ArcaeaScoringMode.B30_R10 -> 2
            ArcaeaScoringMode.B50 -> 3
        }

    Card(modifier) {
        PotentialRow(
            label = stringResource(R.string.arcaea_potential),
            value = ArcaeaFormatters.potentialToText(uiState.potential, mainScale),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(dimensionResource(R.dimen.page_padding)),
            labelTextStyle = MaterialTheme.typography.headlineSmall,
            valueTextStyle = MaterialTheme.typography.displayLarge,
        )

        HorizontalDivider()

        Column(
            Modifier.padding(dimensionResource(R.dimen.page_padding)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
        ) {
            when (uiState.scoringMode) {
                ArcaeaScoringMode.B50 -> {
                    PotentialRow("B10", uiState.entries?.b10?.total, 10, Modifier.fillMaxWidth())
                    PotentialRow("B50", uiState.entries?.b50?.total, 50, Modifier.fillMaxWidth())
                }

                ArcaeaScoringMode.B30_R10 -> {
                    PotentialRow("B30", uiState.entries?.b30?.total, 30, Modifier.fillMaxWidth())
                    PotentialRow("R10", uiState.entries?.r10?.total, 10, Modifier.fillMaxWidth())
                }
            }

            if (warnings.isNotEmpty()) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.extendedColorScheme.warning,
                ) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(painterResource(R.drawable.chart_line_alert), contentDescription = null)

                        Text(
                            warnings.joinToString("\n"),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun OverviewPotentialCardPreview() {
    val entries =
        PotentialGroups(
            b10 = PotentialGroup(items = listOf(), total = 125.0, isComplete = true),
            b50 = PotentialGroup(items = listOf(), total = 630.0, isComplete = false),
            b30 = PotentialGroup(items = listOf(), total = 380.0, isComplete = false),
            r10 = PotentialGroup(items = listOf(), total = 130.0, isComplete = true),
        )
    val scoringMode = ArcaeaScoringMode.B30_R10
    val potential = scoringMode.calculatePotential(entries)

    ArcaeaOfflineTheme {
        Surface {
            OverviewPotentialCard(
                OverviewViewModel.UiState(
                    isLoading = false,
                    scoringMode = scoringMode,
                    entries = entries,
                    potential = potential,
                ),
                Modifier.fillMaxWidth(),
            )
        }
    }
}
