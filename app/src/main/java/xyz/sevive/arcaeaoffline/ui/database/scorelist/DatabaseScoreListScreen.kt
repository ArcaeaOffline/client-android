package xyz.sevive.arcaeaoffline.ui.database.scorelist

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.SubScreenTopAppBar
import xyz.sevive.arcaeaoffline.ui.common.scoreeditor.ScoreEditorDialog
import xyz.sevive.arcaeaoffline.ui.common.scoreeditor.ScoreEditorViewModel
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaScoreCard
import xyz.sevive.arcaeaoffline.ui.utils.potentialToText

@Composable
internal fun DatabaseScoreListItem(
    uiItem: DatabaseScoreListUiItem,
    onRequestEdit: () -> Unit,
    inSelectMode: Boolean,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val (score, scoreCalculated, chart) = uiItem

    Row(
        modifier.clickable(inSelectMode) { onSelectedChange(!selected) },
        verticalAlignment = Alignment.Bottom
    ) {
        ArcaeaScoreCard(
            score = score, Modifier.weight(1f), chart = chart
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PTT", fontWeight = FontWeight.Thin, style = MaterialTheme.typography.labelSmall)
            Text(
                potentialToText(scoreCalculated?.potential),
                style = MaterialTheme.typography.labelMedium,
            )

            AnimatedContent(inSelectMode, label = "") {
                if (it) {
                    Checkbox(checked = selected, onCheckedChange = onSelectedChange)
                } else {
                    IconButton(onClick = onRequestEdit) {
                        Icon(Icons.Default.Edit, null)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DatabaseScoreListScreen(
    onNavigateUp: () -> Unit,
    databaseScoreListViewModel: DatabaseScoreListViewModel = viewModel(factory = AppViewModelProvider.Factory),
    scoreEditorViewModel: ScoreEditorViewModel = viewModel(),
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val uiItems by databaseScoreListViewModel.uiItems.collectAsState()
    var showScoreEditor by rememberSaveable { mutableStateOf(false) }

    var inSelectMode by rememberSaveable { mutableStateOf(false) }
    val selectedItemIds by databaseScoreListViewModel.selectedUiItemIds.collectAsState()
    var showDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }

    val exitSelectMode = {
        databaseScoreListViewModel.clearSelectedItems()
        inSelectMode = false
    }

    BackHandler(inSelectMode) { exitSelectMode() }

    SubScreenContainer(topBar = {
        SubScreenTopAppBar(
            onNavigateUp = {
                if (inSelectMode) exitSelectMode()
                else onNavigateUp()
            },
            title = { Text(stringResource(R.string.database_score_list_title)) },
            actions = {
                AnimatedContent(inSelectMode, label = "selectModeActions") {
                    if (it) {
                        Row {
                            IconButton(
                                onClick = { databaseScoreListViewModel.clearSelectedItems() },
                                enabled = selectedItemIds.isNotEmpty(),
                            ) {
                                Icon(Icons.Default.Deselect, null)
                            }
                            IconButton(
                                onClick = { showDeleteConfirmDialog = true },
                                enabled = selectedItemIds.isNotEmpty(),
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                            ) {
                                Icon(Icons.Default.Delete, null)
                            }
                            IconButton(onClick = { exitSelectMode() }) {
                                Icon(Icons.Default.Close, null)
                            }
                        }
                    } else {
                        IconButton(onClick = { inSelectMode = true }) {
                            Icon(Icons.Default.Checklist, null)
                        }
                    }
                }
            },
        )
    }) {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_arrangement_padding))) {
            items(uiItems, key = { it.id }) {
                val itemSelected = selectedItemIds.contains(it.id)

                Box(Modifier.animateItemPlacement()) {
                    DatabaseScoreListItem(
                        it,
                        {
                            scoreEditorViewModel.setArcaeaScore(it.score)
                            showScoreEditor = true
                        },
                        inSelectMode = inSelectMode,
                        selected = itemSelected,
                        onSelectedChange = { checked ->
                            if (checked) databaseScoreListViewModel.selectItem(it)
                            else databaseScoreListViewModel.deselectItem(it)
                        },
                    )
                }
            }
        }
    }

    if (showScoreEditor) {
        ScoreEditorDialog(
            onDismiss = { showScoreEditor = false },
            onScoreCommit = {
                coroutineScope.launch {
                    databaseScoreListViewModel.updateScore(it)
                }
                Toast.makeText(
                    context, "Update score ${it.id}", Toast.LENGTH_SHORT
                ).show()
                showScoreEditor = false
            },
            scoreEditorViewModel = scoreEditorViewModel,
        )
    }

    if (showDeleteConfirmDialog) {
        val selectedItemsCount = selectedItemIds.size
        val contentColor = MaterialTheme.colorScheme.error

        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch { databaseScoreListViewModel.deleteSelection() }
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = contentColor,
                    ),
                ) {
                    Text(stringResource(R.string.general_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text(stringResource(R.string.general_cancel))
                }
            },
            icon = { Icon(Icons.Default.DeleteForever, null) },
            text = {
                Text(
                    String.format(
                        pluralStringResource(
                            R.plurals.database_score_list_delete_confirm_dialog_content,
                            selectedItemsCount
                        ), selectedItemsCount
                    )
                )
            },
            iconContentColor = contentColor,
            titleContentColor = contentColor,
            textContentColor = contentColor,
        )
    }
}
