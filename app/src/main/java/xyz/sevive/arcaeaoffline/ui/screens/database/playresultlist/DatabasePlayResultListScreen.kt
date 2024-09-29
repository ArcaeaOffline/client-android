package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

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
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
    item: DatabasePlayResultListViewModel.ListItem,
    onPlayResultChange: (PlayResult) -> Unit,
    inSelectMode: Boolean,
    isSelected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPlayResultEditor by rememberSaveable { mutableStateOf(false) }

    if (showPlayResultEditor) {
        PlayResultEditorDialog(
            onDismiss = { showPlayResultEditor = false },
            playResult = item.playResult,
            onPlayResultChange = onPlayResultChange,
        )
    }

    Row(
        modifier.clickable(inSelectMode) { onSelectedChange(!isSelected) },
        verticalAlignment = Alignment.Bottom
    ) {
        ArcaeaPlayResultCard(
            playResult = item.playResult, modifier = Modifier.weight(1f), chart = item.chart
        )

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("PTT", fontWeight = FontWeight.Thin, style = MaterialTheme.typography.labelSmall)
            Text(item.potentialText, style = MaterialTheme.typography.labelMedium)

            Crossfade(inSelectMode, label = "") {
                if (it) {
                    Checkbox(checked = isSelected, onCheckedChange = onSelectedChange)
                } else {
                    IconButton(onClick = { showPlayResultEditor = true }) {
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
    val selectedItemUuids by viewModel.selectedItemUuids.collectAsStateWithLifecycle()

    val uiListItems = uiState.listItems
    val isLoading = uiState.isLoading

    val selectedItemsCount by remember { derivedStateOf { selectedItemUuids.size } }
    var showDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }

    var isInSelectMode by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isInSelectMode) { if (!isInSelectMode) viewModel.clearSelectedItems() }

    BackHandler(isInSelectMode) { isInSelectMode = false }

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
                        IconButton(onClick = { isInSelectMode = true }) {
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
                items(uiListItems, key = { it.uuid }) {
                    val isSelected by remember {
                        derivedStateOf { selectedItemUuids.contains(it.uuid) }
                    }

                    DatabasePlayResultListItem(
                        item = it,
                        onPlayResultChange = { newPlayResult ->
                            viewModel.updatePlayResult(newPlayResult)

                            Toast.makeText(
                                context,
                                "Update playResult ${newPlayResult.uuid}",
                                Toast.LENGTH_SHORT,
                            ).show()
                        },
                        inSelectMode = isInSelectMode,
                        isSelected = isSelected,
                        onSelectedChange = { selected -> viewModel.setItemSelected(it, selected) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            confirmButton = {
                DialogConfirmButton(
                    onClick = {
                        viewModel.deleteSelectedItemsInDatabase()
                        showDeleteConfirmDialog = false
                    },
                    colors = DialogConfirmButtonDefaults.dangerColors,
                )
            },
            dismissButton = {
                DialogDismissTextButton(onClick = { showDeleteConfirmDialog = false })
            },
            icon = {
                BadgedBox(badge = { Badge { Text(selectedItemsCount.toString()) } }) {
                    Icon(Icons.Default.DeleteForever, null)
                }
            },
            title = { Text(stringResource(R.string.general_delete)) },
            text = {
                Text(
                    pluralStringResource(
                        R.plurals.database_play_result_list_delete_confirm_dialog_content,
                        selectedItemsCount,
                        selectedItemsCount
                    )
                )
            },
            iconContentColor = MaterialTheme.colorScheme.error,
            titleContentColor = MaterialTheme.colorScheme.error,
        )
    }
}
