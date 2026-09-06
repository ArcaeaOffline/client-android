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
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.Difficulty
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaChartCard
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaChartSelector
import xyz.sevive.arcaeaoffline.ui.components.BasicAlertDialogSurface
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.rememberArcaeaChartDisplay
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

@Composable
private fun SelectChartDialog(
    onDismiss: () -> Unit,
    songId: String?,
    ratingClass: ArcaeaRatingClass?,
    onDifficultyChange: (Difficulty) -> Unit,
) {
    BasicAlertDialogSurface(onDismissRequest = onDismiss) {
        ArcaeaChartSelector(
            songId = songId,
            ratingClass = ratingClass,
            onDifficultyChange = onDifficultyChange,
        )
    }
}

@Composable
internal fun DatabaseAddPlayResultChartAction(
    difficulty: Difficulty?,
    onDifficultyChange: (Difficulty) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showSelectChartDialog by rememberSaveable { mutableStateOf(false) }

    if (showSelectChartDialog) {
        SelectChartDialog(
            onDismiss = { showSelectChartDialog = false },
            songId = difficulty?.songId,
            ratingClass = difficulty?.ratingClass,
            onDifficultyChange = onDifficultyChange,
        )
    }

    val display by rememberArcaeaChartDisplay(difficulty?.songId, difficulty?.ratingClass)

    Row(modifier, verticalAlignment = Alignment.Bottom) {
        Box(modifier = Modifier.weight(1f)) {
            if (display != null) {
                ArcaeaChartCard(
                    display!!.difficultyWithSong,
                    Modifier.fillMaxWidth(),
                    chartInfo = display!!.chartInfo,
                )
            } else {
                Card(
                    onClick = { showSelectChartDialog = true },
                    Modifier.fillMaxWidth(),
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                        IconRow(
                            Modifier
                                .minimumInteractiveComponentSize()
                                .padding(dimensionResource(R.dimen.card_padding)),
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
    val difficulty =
        Difficulty(
            songId = "test",
            ratingClass = ArcaeaRatingClass.FUTURE,
            ratingClassAlias = null,
            rating = 9,
            ratingPlus = true,
            chartDesigner = null,
            jacketDesigner = null,
            audioOverride = false,
            jacketOverride = false,
            jacketNight = null,
            title = "Preview",
            artist = "Preview",
            bg = null,
            bgInverse = null,
            bpm = null,
            bpmBase = null,
            version = null,
            date = null,
        )

    ArcaeaOfflineTheme {
        Surface {
            Column {
                DatabaseAddPlayResultChartAction(
                    difficulty = null,
                    onDifficultyChange = {},
                    modifier = Modifier.fillMaxWidth(),
                )

                DatabaseAddPlayResultChartAction(
                    difficulty = difficulty,
                    onDifficultyChange = {},
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
