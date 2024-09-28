package xyz.sevive.arcaeaoffline.ui.screens.database.addplayresult

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.window.Dialog
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaChartCard
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaChartSelector
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@Composable
private fun SelectChartDialog(
    onDismiss: () -> Unit,
    chart: Chart?,
    onChartChange: (Chart?) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface {
            ArcaeaChartSelector(
                chart = chart,
                onChartChange = onChartChange,
                allowFakeChart = true,
            )
        }
    }
}

@Composable
internal fun DatabaseAddPlayResultChartAction(
    chart: Chart?,
    onChartChange: (Chart?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSelectChartDialog by rememberSaveable { mutableStateOf(false) }

    if (showSelectChartDialog) {
        SelectChartDialog(
            onDismiss = { showSelectChartDialog = false },
            chart = chart,
            onChartChange = { onChartChange(it) },
        )
    }

    Row(modifier, verticalAlignment = Alignment.Bottom) {
        Box(modifier = Modifier.weight(1f)) {
            if (chart != null) {
                ArcaeaChartCard(chart = chart, Modifier.fillMaxWidth())
            } else {
                Card(
                    onClick = { showSelectChartDialog = true },
                    Modifier.fillMaxWidth(),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                        IconRow(
                            Modifier
                                .minimumInteractiveComponentSize()
                                .padding(dimensionResource(R.dimen.card_padding))
                        ) {
                            Icon(Icons.Default.TouchApp, null)
                            Text(stringResource(R.string.database_add_play_result_click_select_chart))
                        }
                    }
                }
            }
        }

        IconButton(onClick = { showSelectChartDialog = true }) {
            Icon(Icons.Default.Edit, contentDescription = null)
        }
    }
}

@PreviewLightDark
@Composable
private fun PlayResultActionPreview() {
    val chart = Chart(
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
    )

    ArcaeaOfflineTheme {
        Surface {
            Column {
                DatabaseAddPlayResultChartAction(
                    chart = null,
                    onChartChange = {},
                    modifier = Modifier.fillMaxWidth(),
                )

                DatabaseAddPlayResultChartAction(
                    chart = chart,
                    onChartChange = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
