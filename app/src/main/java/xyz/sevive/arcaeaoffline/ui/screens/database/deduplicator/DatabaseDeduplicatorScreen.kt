package xyz.sevive.arcaeaoffline.ui.screens.database.deduplicator

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Deselect
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.LoadingOverlay
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseScreenDestinations
import xyz.sevive.arcaeaoffline.ui.screens.EmptyScreen
import xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.DatabasePlayResultDeleteConfirmDialog


@Composable
fun DatabaseDeduplicatorScreen(
    onNavigateUp: () -> Unit,
    vm: DatabaseDeduplicatorViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val groupByValues by vm.groupByValues.collectAsStateWithLifecycle()
    val selectedUuids by vm.selectedUuids.collectAsStateWithLifecycle()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    val showEmptyScreen by remember {
        derivedStateOf { uiState.listItems.isEmpty() }
    }
    val selectionIsNotEmpty by remember {
        derivedStateOf { selectedUuids.isNotEmpty() }
    }

    var showGroupByValuesSelectDialog by rememberSaveable { mutableStateOf(false) }
    if (showGroupByValuesSelectDialog) {
        DatabaseDeduplicatorGroupByValuesSelectDialog(
            onDismissRequest = { showGroupByValuesSelectDialog = false },
            values = groupByValues,
            onValuesChange = {
                vm.setGroupByValues(it)
                vm.buildDuplicateGroups(it)
            },
        )
    }

    var showWizardDialog by rememberSaveable { mutableStateOf(false) }
    if (showWizardDialog) {
        DatabaseDeduplicatorWizardDialog(
            onDismissRequest = { showWizardDialog = false },
            onAutoSelect = { vm.autoSelect(it) },
            onAutoMerge = { vm.autoMerge() },
        )
    }

    var showDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }
    if (showDeleteConfirmDialog) {
        // TODO: split general delete confirm dialog...?
        DatabasePlayResultDeleteConfirmDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            onConfirm = {
                vm.deleteSelectedItemsInDatabase(selectedUuids)
                showDeleteConfirmDialog = false
            },
            selectedItemUuids = selectedUuids.toList(),
        )
    }

    SubScreenContainer(
        onNavigateUp = { onNavigateUp() },
        title = stringResource(DatabaseScreenDestinations.Deduplicator.title),
        actions = {
            IconButton(
                onClick = { vm.buildDuplicateGroups(groupByValues) },
                enabled = !uiState.isLoading,
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
            }
            IconButton(
                onClick = { showWizardDialog = true },
                enabled = !uiState.isLoading,
            ) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null)
            }
            IconButton(
                onClick = { showGroupByValuesSelectDialog = true },
                enabled = !uiState.isLoading,
            ) {
                Icon(Icons.Default.Tune, contentDescription = null)
            }
            IconButton(
                onClick = { vm.clearSelectedItems() },
                enabled = !uiState.isLoading && selectionIsNotEmpty,
            ) {
                Icon(Icons.Default.Deselect, contentDescription = null)
            }
            IconButton(
                onClick = { showDeleteConfirmDialog = true },
                enabled = !uiState.isLoading && selectionIsNotEmpty,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(Icons.Default.Delete, contentDescription = null)
            }
        },
    ) {
        LoadingOverlay(loading = uiState.isLoading) {
            Crossfade(targetState = showEmptyScreen, label = "emptyScreenCrossfade") { state ->
                if (state) {
                    EmptyScreen(Modifier.fillMaxSize())
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(all = dimensionResource(R.dimen.page_padding)),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
                    ) {
                        items(uiState.listItems, key = { it.key }) { item ->
                            DatabaseDeduplicatorGroupListItem(
                                item,
                                selectedUuids = selectedUuids,
                                onPlayResultSelectedChange = { uuid, selected ->
                                    vm.setPlayResultSelected(uuid, selected)
                                },
                                onMergeConfirm = { newPlayResult ->
                                    vm.mergeGroup(item.key, newPlayResult)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}
