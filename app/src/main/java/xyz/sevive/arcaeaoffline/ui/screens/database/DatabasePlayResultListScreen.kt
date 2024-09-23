package xyz.sevive.arcaeaoffline.ui.screens.database

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.SubScreenTopAppBar
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaPlayResultCard
import xyz.sevive.arcaeaoffline.ui.components.PlayResultEditorDialog
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButton
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButtonDefaults
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogDismissTextButton
import xyz.sevive.arcaeaoffline.ui.screens.EmptyScreen

@Composable
internal fun DatabasePlayResultListItem(
    uiItem: DatabasePlayResultListViewModel.UiItem,
    onScoreChange: (PlayResult) -> Unit,
    inSelectMode: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showScoreEditor by rememberSaveable { mutableStateOf(false) }

    var lastSavedPlayResult by remember { mutableStateOf(uiItem.playResult) }
    if (showScoreEditor) {
        PlayResultEditorDialog(
            onDismiss = {
                if (uiItem.playResult != lastSavedPlayResult) onScoreChange(lastSavedPlayResult)
                showScoreEditor = false
            },
            playResult = lastSavedPlayResult,
            onPlayResultChange = { lastSavedPlayResult = it },
        )
    }

    Row(
        modifier.clickable(inSelectMode) { onSelectedChange(!uiItem.selected) },
        verticalAlignment = Alignment.Bottom
    ) {
        ArcaeaPlayResultCard(
            playResult = uiItem.playResult, modifier = Modifier.weight(1f), chart = uiItem.chart
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PTT", fontWeight = FontWeight.Thin, style = MaterialTheme.typography.labelSmall)
            Text(uiItem.potentialText, style = MaterialTheme.typography.labelMedium)

            Crossfade(inSelectMode, label = "") {
                if (it) {
                    Checkbox(checked = uiItem.selected, onCheckedChange = onSelectedChange)
                } else {
                    IconButton(onClick = { showScoreEditor = true }) {
                        Icon(Icons.Default.Edit, null)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabasePlayResultListScreen(
    onNavigateUp: () -> Unit,
    viewModel: DatabasePlayResultListViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    @Suppress("UNUSED_VARIABLE") val databaseLastUpdated by viewModel.databaseListenFlow.collectAsStateWithLifecycle()

    val uiListItems = uiState.uiListItems
    val isInSelectMode = uiState.isInSelectMode
    val isLoading = uiState.isLoading

    val selectedItemsCount = uiListItems.count { it.selected }
    var showDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler(isInSelectMode) { viewModel.exitSelectMode() }

    SubScreenContainer(topBar = {
        SubScreenTopAppBar(
            onNavigateUp = { onNavigateUp() },
            title = { Text(stringResource(R.string.database_play_result_list_title)) },
            actions = {
                AnimatedContent(isInSelectMode, label = "selectModeActions") {
                    if (it) {
                        Row {
                            IconButton(
                                onClick = { viewModel.clearSelectedItems() },
                                enabled = selectedItemsCount > 0,
                            ) {
                                Icon(Icons.Default.Deselect, null)
                            }
                            IconButton(
                                onClick = { showDeleteConfirmDialog = true },
                                enabled = selectedItemsCount > 0,
                                colors = IconButtonDefaults.iconButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error,
                                ),
                            ) {
                                Icon(Icons.Default.Delete, null)
                            }
                        }
                    } else {
                        IconButton(onClick = { viewModel.enterSelectMode() }) {
                            Icon(Icons.Default.Checklist, null)
                        }
                    }
                }
            },
        )
    }) {
        if (isLoading) {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        } else if (uiListItems.isEmpty()) {
            EmptyScreen(Modifier.fillMaxSize())
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))) {
                items(uiListItems, key = { it.id }) {
                    DatabasePlayResultListItem(
                        uiItem = it,
                        onScoreChange = { scoreEdited ->
                            viewModel.updateScore(scoreEdited)

                            Toast.makeText(
                                context, "Update playResult ${it.id}", Toast.LENGTH_SHORT
                            ).show()
                        },
                        inSelectMode = isInSelectMode,
                        onSelectedChange = { selected -> viewModel.setItemSelected(it, selected) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        val contentColor = MaterialTheme.colorScheme.error

        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            confirmButton = {
                DialogConfirmButton(
                    onClick = {
                        viewModel.deleteSelectedItems()
                        showDeleteConfirmDialog = false
                    },
                    colors = DialogConfirmButtonDefaults.dangerColors,
                )
            },
            dismissButton = {
                DialogDismissTextButton(onClick = { showDeleteConfirmDialog = false })
            },
            icon = { Icon(Icons.Default.DeleteForever, null) },
            text = {
                Text(
                    pluralStringResource(
                        R.plurals.database_play_result_list_delete_confirm_dialog_content,
                        selectedItemsCount,
                        selectedItemsCount
                    )
                )
            },
            iconContentColor = contentColor,
            titleContentColor = contentColor,
            textContentColor = contentColor,
        )
    }
}
