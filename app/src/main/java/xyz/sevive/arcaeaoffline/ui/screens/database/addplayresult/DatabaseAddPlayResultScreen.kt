package xyz.sevive.arcaeaoffline.ui.screens.database.addplayresult

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.IconRow
import xyz.sevive.arcaeaoffline.ui.components.ListGroupHeader
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseScreenDestinations

@Composable
internal fun BottomActionsBar(
    onReset: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(modifier) {
        OutlinedButton(
            onClick = onReset,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            IconRow {
                Icon(Icons.Default.RestartAlt, contentDescription = null)
                Text(stringResource(R.string.general_reset))
            }
        }

        Spacer(Modifier.weight(1f))

        Button(onClick = onSave, enabled = saveEnabled) {
            IconRow {
                Icon(Icons.Default.Save, contentDescription = null)
                Text(stringResource(R.string.general_save))
            }
        }
    }
}

@Composable
internal fun DatabaseAddPlayResultScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DatabaseAddPlayResultViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playResultEditEnabled by remember { derivedStateOf { uiState.playResult != null } }

    SubScreenContainer(
        onNavigateUp = onNavigateUp,
        modifier = modifier,
        title = stringResource(DatabaseScreenDestinations.AddPlayResult.title),
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                BottomActionsBar(
                    onReset = { viewModel.reset() },
                    onSave = { viewModel.savePlayResult() },
                    saveEnabled = playResultEditEnabled,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(dimensionResource(R.dimen.page_padding)),
                )
            },
        ) { innerPadding ->
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding),
                contentPadding = innerPadding,
            ) {
                item {
                    ListGroupHeader(stringResource(R.string.database_add_play_result_select_chart_header))
                }

                item {
                    DatabaseAddPlayResultChartAction(
                        chart = uiState.chart,
                        onChartChange = { viewModel.setChart(it) },
                        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_group_header_horizontal_padding)),
                    )
                }

                item {
                    // Just adding a padding
                    Box(Modifier.padding(vertical = dimensionResource(R.dimen.list_padding))) {}
                }

                item {
                    ListGroupHeader(stringResource(R.string.database_add_play_result_edit_play_result_header))
                }

                item {
                    DatabaseAddPlayResultPlayResultAction(
                        playResult = uiState.playResult,
                        onPlayResultChange = { viewModel.setPlayResult(it) },
                        warnings = uiState.warnings,
                        modifier = Modifier.padding(horizontal = dimensionResource(R.dimen.list_group_header_horizontal_padding)),
                    )
                }
            }
        }
    }
}
