package xyz.sevive.arcaeaoffline.ui.screens.database.addplayresult

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoringMode
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.entities.playRating
import xyz.sevive.arcaeaoffline.core.validation.PlayResultFinding
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.LabeledCheckbox
import xyz.sevive.arcaeaoffline.ui.components.PlayResultEditorClearTypeField
import xyz.sevive.arcaeaoffline.ui.components.PlayResultEditorCommentField
import xyz.sevive.arcaeaoffline.ui.components.PlayResultEditorDateTimeField
import xyz.sevive.arcaeaoffline.ui.components.PlayResultEditorFarField
import xyz.sevive.arcaeaoffline.ui.components.PlayResultEditorLostField
import xyz.sevive.arcaeaoffline.ui.components.PlayResultEditorMaxRecallField
import xyz.sevive.arcaeaoffline.ui.components.PlayResultEditorModifierField
import xyz.sevive.arcaeaoffline.ui.components.PlayResultEditorPureField
import xyz.sevive.arcaeaoffline.ui.components.PlayResultWarningDetailsDialog
import xyz.sevive.arcaeaoffline.ui.components.SaveWithWarningsDialog
import xyz.sevive.arcaeaoffline.ui.components.SettingsGroupHeader
import xyz.sevive.arcaeaoffline.ui.components.TextItem
import xyz.sevive.arcaeaoffline.ui.components.arcaea.OutlinedArcaeaScoreTextField
import xyz.sevive.arcaeaoffline.ui.components.arcaea.rememberArcaeaScoreTextFieldState
import xyz.sevive.arcaeaoffline.ui.helpers.ArcaeaFormatters
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseSubScreen
import xyz.sevive.arcaeaoffline.ui.theme.extendedColorScheme
import xyz.sevive.arcaeaoffline.ui.theme.spacing

@Composable
internal fun BottomActionsBar(
    warnings: List<PlayResultFinding>,
    keepChart: Boolean,
    onKeepChartChange: (Boolean) -> Unit,
    onReset: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    var showWarningsDialog by rememberSaveable { mutableStateOf(false) }
    if (showWarningsDialog && warnings.isNotEmpty()) {
        PlayResultWarningDetailsDialog(
            onDismissRequest = { showWarningsDialog = false },
            warnings = warnings,
        )
    }
    LaunchedEffect(warnings) { if (warnings.isEmpty()) showWarningsDialog = false }

    Surface(modifier, color = BottomAppBarDefaults.containerColor) {
        Column {
            if (warnings.isNotEmpty()) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.extendedColorScheme.onWarningContainer,
                ) {
                    TextItem(
                        title = pluralStringResource(R.plurals.play_result_validator_warning_count, warnings.size, warnings.size),
                        leadingIcon = Icons.Default.Warning,
                        leadingIconTint = MaterialTheme.extendedColorScheme.onWarningContainer,
                        onClick = { showWarningsDialog = true },
                        modifier = Modifier.background(MaterialTheme.extendedColorScheme.warningContainer.copy(alpha = 0.5f)),
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(MaterialTheme.spacing.pagePadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onReset,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text(stringResource(R.string.general_reset))
                }

                Spacer(Modifier.weight(1f))

                LabeledCheckbox(
                    checked = keepChart,
                    onCheckedChange = onKeepChartChange,
                    label = stringResource(R.string.database_add_play_result_keep_chart),
                )

                Spacer(Modifier.width(MaterialTheme.spacing.sm))

                Button(onClick = onSave, enabled = saveEnabled) {
                    Text(stringResource(R.string.general_save))
                }
            }
        }
    }
}

@Composable
private fun PlayRatingPreviewLine(
    playResult: PlayResult,
    scoringMode: ArcaeaScoringMode,
    chartInfo: ChartInfo?,
    modifier: Modifier = Modifier,
) {
    val ratingText =
        remember(playResult, scoringMode, chartInfo) {
            ArcaeaFormatters.potentialToText(chartInfo?.let { playResult.playRating(it, scoringMode) })
        }

    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(R.string.arcaea_play_rating),
            style = MaterialTheme.typography.labelLarge,
            modifier = Modifier.secondaryItemAlpha(),
        )

        Text(ratingText, style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
internal fun DatabaseAddPlayResultScreen(
    modifier: Modifier = Modifier,
    viewModel: DatabaseAddPlayResultViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val playResult = uiState.playResult
    val saveEnabled = playResult != null

    val scoreTextFieldState = rememberArcaeaScoreTextFieldState(playResult?.score ?: 0)

    LaunchedEffect(scoreTextFieldState.intValue) {
        scoreTextFieldState.intValue?.let { score ->
            playResult?.let { viewModel.setPlayResult(it.copy(score = score)) }
        }
    }

    LaunchedEffect(playResult?.score) {
        val score = playResult?.score ?: 0
        if (scoreTextFieldState.intValue != score) {
            scoreTextFieldState.updateValue(score)
        }
    }

    val resources = LocalResources.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            snackbarHostState.showSnackbar(event.asString(resources), withDismissAction = true)
        }
    }

    var showSaveConfirmDialog by rememberSaveable { mutableStateOf(false) }

    fun requestSave() {
        if (uiState.warnings.isEmpty()) {
            viewModel.savePlayResult()
        } else {
            showSaveConfirmDialog = true
        }
    }

    if (showSaveConfirmDialog) {
        SaveWithWarningsDialog(
            warnings = uiState.warnings,
            onConfirm = {
                showSaveConfirmDialog = false
                viewModel.savePlayResult()
            },
            onDismissRequest = { showSaveConfirmDialog = false },
        )
    }

    SubScreenContainer(
        modifier = modifier,
        title = stringResource(DatabaseSubScreen.AddPlayResult.title),
    ) {
        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            bottomBar = {
                BottomActionsBar(
                    warnings = uiState.warnings,
                    keepChart = uiState.keepChartAfterSave,
                    onKeepChartChange = viewModel::setKeepChartAfterSave,
                    onReset = viewModel::reset,
                    onSave = ::requestSave,
                    saveEnabled = saveEnabled,
                    modifier = Modifier.fillMaxWidth(),
                )
            },
        ) { innerPadding ->
            Column(
                Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(innerPadding)
                    .padding(bottom = MaterialTheme.spacing.pagePadding),
            ) {
                SettingsGroupHeader(stringResource(R.string.database_add_play_result_select_chart_header))

                DatabaseAddPlayResultChartAction(
                    chart = uiState.chart,
                    chartInfo = uiState.chartInfo,
                    onDifficultyChange = { viewModel.selectChart(it.songId, it.ratingClass) },
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.lg),
                )

                SettingsGroupHeader(stringResource(R.string.database_add_play_result_edit_play_result_header))

                Column(
                    Modifier.padding(horizontal = MaterialTheme.spacing.pagePadding),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
                ) {
                    if (playResult == null) {
                        Text(
                            stringResource(R.string.database_add_play_result_select_chart_first),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.secondaryItemAlpha(),
                        )
                    } else {
                        OutlinedArcaeaScoreTextField(scoreTextFieldState, Modifier.fillMaxWidth())

                        PlayRatingPreviewLine(playResult, uiState.scoringMode, uiState.chartInfo)

                        PlayResultEditorPureField(
                            pure = playResult.pure,
                            onPureChange = { viewModel.setPlayResult(playResult.copy(pure = it)) },
                        )

                        PlayResultEditorFarField(
                            far = playResult.far,
                            onFarChange = { viewModel.setPlayResult(playResult.copy(far = it)) },
                        )

                        PlayResultEditorLostField(
                            lost = playResult.lost,
                            onLostChange = { viewModel.setPlayResult(playResult.copy(lost = it)) },
                        )

                        PlayResultEditorMaxRecallField(
                            maxRecall = playResult.maxRecall,
                            onMaxRecallChange = { viewModel.setPlayResult(playResult.copy(maxRecall = it)) },
                        )

                        PlayResultEditorClearTypeField(
                            clearType = playResult.clearType,
                            onClearTypeChange = { viewModel.setPlayResult(playResult.copy(clearType = it)) },
                        )

                        PlayResultEditorModifierField(
                            arcaeaModifier = playResult.modifier,
                            onArcaeaModifierChange = { viewModel.setPlayResult(playResult.copy(modifier = it)) },
                        )

                        PlayResultEditorDateTimeField(
                            instant = playResult.date,
                            onInstantChange = { viewModel.setPlayResult(playResult.copy(date = it)) },
                        )

                        PlayResultEditorCommentField(
                            comment = playResult.comment,
                            onCommentChange = { viewModel.setPlayResult(playResult.copy(comment = it)) },
                        )
                    }
                }
            }
        }
    }
}
