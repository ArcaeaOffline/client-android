package xyz.sevive.arcaeaoffline.ui.database

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaChartCard
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultCard
import xyz.sevive.arcaeaoffline.ui.components.ChartSelector
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.PlayResultEditorDialog
import xyz.sevive.arcaeaoffline.ui.components.PlayResultValidatorWarningDetails


@Composable
fun SelectChartDialog(
    onDismiss: () -> Unit,
    chart: Chart?,
    onChartChange: (Chart?) -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface {
            Card {
                ChartSelector(chart = chart, onChartChange = onChartChange)
            }
        }
    }
}

@Composable
internal fun DelimiterWithText(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text)
        HorizontalDivider(Modifier.padding(start = 20.dp))
    }
}

@Composable
internal fun ChartAction(chart: Chart?, onOpenSelectChartDialogRequest: () -> Unit) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        if (chart != null) {
            ArcaeaChartCard(chart = chart, Modifier.weight(1f))
        } else {
            Card(
                onClick = onOpenSelectChartDialogRequest,
                Modifier.weight(1f),
            ) {
                IconRow(
                    modifier = Modifier.padding(dimensionResource(R.dimen.card_padding)),
                    icon = {
                        Icon(Icons.Default.TouchApp, null)
                    },
                ) {
                    Text(stringResource(R.string.database_add_play_result_click_select_chart))
                }
            }
        }

        IconButton(onClick = onOpenSelectChartDialogRequest) {
            Icon(Icons.Default.Edit, contentDescription = null)
        }
    }
}

@Composable
internal fun ScoreWarningsCard(
    warnings: List<ArcaeaPlayResultValidatorWarning>,
    modifier: Modifier = Modifier,
) {
    var showWarningsDialog by rememberSaveable { mutableStateOf(false) }

    if (showWarningsDialog) {
        Dialog(onDismissRequest = { showWarningsDialog = false }) {
            Surface {
                Card {
                    PlayResultValidatorWarningDetails(
                        warnings = warnings,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }

    Card(onClick = { showWarningsDialog = true }, modifier = modifier) {
        IconRow(
            modifier = Modifier.padding(dimensionResource(R.dimen.card_padding)),
            icon = {
                Icon(Icons.Default.Warning, null)
            },
        ) {
            Text(
                pluralStringResource(
                    R.plurals.play_result_validator_warning_count,
                    warnings.size,
                    warnings.size
                )
            )
        }
    }
}

@Composable
internal fun ScoreAction(
    playResult: PlayResult?,
    scoreEditEnabled: Boolean,
    onOpenScoreEditorDialogRequest: () -> Unit,
    warnings: List<ArcaeaPlayResultValidatorWarning>,
) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
    ) {
        if (playResult != null) {
            Column(Modifier.weight(1f)) {
                AnimatedVisibility(warnings.isNotEmpty()) {
                    ScoreWarningsCard(
                        warnings = warnings,
                        Modifier.fillMaxWidth()
                    )
                }

                ArcaeaPlayResultCard(playResult = playResult)
            }
        } else {
            Card(Modifier.weight(1f)) {
                IconRow(
                    modifier = Modifier.padding(dimensionResource(R.dimen.card_padding)),
                    icon = {
                        Icon(Icons.Default.Block, null)
                    },
                ) {
                    Text(stringResource(R.string.database_add_play_result_select_chart_first))
                }
            }
        }

        IconButton(
            onClick = onOpenScoreEditorDialogRequest,
            enabled = scoreEditEnabled,
        ) {
            Icon(Icons.Default.Edit, contentDescription = null)
        }
    }
}

@Composable
internal fun BottomActionsBar(
    onReset: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier.padding(bottom = 4.dp)) {
        OutlinedButton(
            onClick = onReset,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            IconRow(icon = { Icon(Icons.Default.RestartAlt, contentDescription = null) }) {
                Text(stringResource(R.string.general_reset))
            }
        }

        Spacer(Modifier.weight(1f))

        Button(onClick = onSave, enabled = saveEnabled) {
            IconRow(icon = { Icon(Icons.Default.Save, contentDescription = null) }) {
                Text(stringResource(R.string.general_save))
            }
        }
    }
}

@Composable
fun DatabaseAddPlayResultScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DatabaseAddPlayResultViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val coroutineScope = rememberCoroutineScope()

    val chart by viewModel.chart.collectAsStateWithLifecycle()
    val score by viewModel.score.collectAsStateWithLifecycle()

    val scoreWarnings by viewModel.scoreWarnings.collectAsStateWithLifecycle()

    val scoreEditEnabled = score != null

    var showSelectChartDialog by rememberSaveable { mutableStateOf(false) }
    var showScoreEditorDialog by rememberSaveable { mutableStateOf(false) }

    if (showSelectChartDialog) {
        SelectChartDialog(
            onDismiss = { showSelectChartDialog = false },
            chart = chart,
            onChartChange = { viewModel.setChart(it) },
        )
    }

    if (showScoreEditorDialog && scoreEditEnabled) {
        PlayResultEditorDialog(
            onDismiss = { showScoreEditorDialog = false },
            playResult = score!!,
            onPlayResultChange = { viewModel.setScore(it) },
        )
    }

    SubScreenContainer(
        onNavigateUp = onNavigateUp, title = stringResource(R.string.database_add_play_result_title)
    ) {
        Scaffold(modifier = modifier, bottomBar = {
            BottomActionsBar(
                onReset = { viewModel.reset() },
                onSave = { coroutineScope.launch { viewModel.saveScore() } },
                saveEnabled = scoreEditEnabled,
                modifier = Modifier.fillMaxWidth(),
            )
        }) {
            LazyColumn(Modifier.padding(it)) {
                item {
                    DelimiterWithText(stringResource(R.string.database_add_play_result_select_chart_header))
                }

                item {
                    ChartAction(
                        chart = chart,
                        onOpenSelectChartDialogRequest = { showSelectChartDialog = true },
                    )
                }

                item {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = dimensionResource(R.dimen.page_padding)),
                    ) {}
                }

                item {
                    DelimiterWithText(stringResource(R.string.database_add_play_result_edit_score_header))
                }

                item {
                    ScoreAction(
                        playResult = score,
                        scoreEditEnabled = scoreEditEnabled,
                        onOpenScoreEditorDialogRequest = { showScoreEditorDialog = true },
                        warnings = scoreWarnings,
                    )
                }
            }
        }
    }
}
