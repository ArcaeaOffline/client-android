package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.screens.EmptyScreen


@Composable
private fun DatabasePlayResultListAppBarActions(
    inSelectMode: Boolean,
    selectedItemsCount: Int,
    onShowDeleteConfirmDialogChange: (Boolean) -> Unit,
    onSelectedModeChange: (Boolean) -> Unit,
    onClearSelectedItems: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedContent(targetState = inSelectMode, modifier = modifier, label = "selectModeActions") {
        if (it) {
            Row {
                IconButton(
                    onClick = onClearSelectedItems,
                    enabled = selectedItemsCount > 0,
                ) {
                    Icon(Icons.Default.Deselect, null)
                }
                IconButton(
                    onClick = { onShowDeleteConfirmDialogChange(true) },
                    enabled = selectedItemsCount > 0,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Icon(Icons.Default.Delete, null)
                }
            }
        } else {
            IconButton(onClick = { onSelectedModeChange(true) }) {
                Icon(Icons.Default.Checklist, null)
            }
        }
    }
}

@Composable
fun DatabasePlayResultListScreen(
    onNavigateUp: () -> Unit,
    viewModel: DatabasePlayResultListViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedItemUuids by viewModel.selectedItemUuids.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val listItems = uiState.listItems
    val isLoading = uiState.isLoading

    val selectedItemsCount by remember { derivedStateOf { selectedItemUuids.size } }

    var isInSelectMode by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(isInSelectMode) { if (!isInSelectMode) viewModel.clearSelectedItems() }
    BackHandler(isInSelectMode) { isInSelectMode = false }

    var showDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }
    if (showDeleteConfirmDialog) {
        DatabasePlayResultDeleteConfirmDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            onConfirm = {
                viewModel.deleteSelectedItemsInDatabase()
                showDeleteConfirmDialog = false
            },
            selectedItemUuids = selectedItemUuids,
        )
    }

    SubScreenContainer(
        onNavigateUp = { onNavigateUp() },
        title = stringResource(R.string.database_play_result_list_title),
        actions = {
            DatabasePlayResultListAppBarActions(
                inSelectMode = isInSelectMode,
                selectedItemsCount = selectedItemsCount,
                onShowDeleteConfirmDialogChange = { showDeleteConfirmDialog = it },
                onSelectedModeChange = { isInSelectMode = it },
                onClearSelectedItems = { viewModel.clearSelectedItems() },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        when {
            isLoading -> Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }

            listItems.isEmpty() -> EmptyScreen(Modifier.fillMaxSize())

            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(all = dimensionResource(R.dimen.page_padding)),
                    verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
                ) {
                    items(listItems, key = { it.uuid }) {
                        val isSelected by remember {
                            derivedStateOf { selectedItemUuids.contains(it.uuid) }
                        }

                        DatabasePlayResultListItem(
                            item = it,
                            onPlayResultChange = { newPlayResult ->
                                viewModel.updatePlayResult(
                                    playResult = newPlayResult,
                                    context = context,
                                    snackbarHostState = snackbarHostState,
                                )
                            },
                            inSelectMode = isInSelectMode,
                            selected = isSelected,
                            onSelectedChange = { selected ->
                                viewModel.setItemSelected(it, selected)
                            },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}
