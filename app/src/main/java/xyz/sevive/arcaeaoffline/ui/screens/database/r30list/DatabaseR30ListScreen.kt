package xyz.sevive.arcaeaoffline.ui.screens.database.r30list


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedDateTime
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.SubScreenTopAppBar
import xyz.sevive.arcaeaoffline.ui.components.LinearProgressIndicatorWrapper
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButton
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogConfirmButtonDefaults
import xyz.sevive.arcaeaoffline.ui.components.dialogs.DialogDismissTextButton
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseScreenDestinations
import xyz.sevive.arcaeaoffline.ui.screens.EmptyScreen

@Composable
private fun DatabaseR30UpdateProgress(current: Int, total: Int, modifier: Modifier = Modifier) {
    LinearProgressIndicatorWrapper(current = current, total = total, modifier)
}

@Composable
private fun DatabaseR30RebuildConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            DialogConfirmButton(
                onClick = onConfirm,
                colors = DialogConfirmButtonDefaults.dangerColors,
            )
        },
        dismissButton = { DialogDismissTextButton(onClick = onDismiss) },
        icon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Icon(Icons.Default.Sync, contentDescription = null)
            }
        },
        title = { Text(stringResource(R.string.database_r30_rebuild_dialog_title)) },
        text = { Text(stringResource(R.string.database_r30_rebuild_dialog_content)) },
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DatabaseR30ListScreen(
    onNavigateUp: () -> Unit,
    viewModel: DatabaseR30ListViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val updateProgress by viewModel.updateProgress.collectAsStateWithLifecycle()
    val isUpdating by remember { derivedStateOf { updateProgress.second > -1 } }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uiItems = uiState.listItems

    val lastUpdatedAtText = remember(uiState.lastUpdatedAt) {
        uiState.lastUpdatedAt?.formatAsLocalizedDateTime() ?: "-"
    }

    var showRebuildConfirmDialog by rememberSaveable { mutableStateOf(false) }
    if (showRebuildConfirmDialog) {
        DatabaseR30RebuildConfirmDialog(
            onDismiss = { showRebuildConfirmDialog = false },
            onConfirm = {
                viewModel.requestRebuild()
                showRebuildConfirmDialog = false
            },
        )
    }

    SubScreenContainer(
        topBar = {
            SubScreenTopAppBar(
                onNavigateUp = onNavigateUp,
                title = {
                    Column {
                        Text(stringResource(DatabaseScreenDestinations.R30.title))
                        Text(
                            stringResource(R.string.general_updated_at, lastUpdatedAtText),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showRebuildConfirmDialog = true },
                        enabled = !isUpdating,
                    ) {
                        Icon(Icons.Default.SyncProblem, null)
                    }

                    IconButton(
                        onClick = { viewModel.requestUpdate() },
                        enabled = !isUpdating,
                    ) {
                        Icon(Icons.Default.Sync, null)
                    }
                },
            )
        },
    ) {
        if (isUpdating) {
            Box(Modifier.fillMaxSize()) {
                DatabaseR30UpdateProgress(
                    current = updateProgress.first,
                    total = updateProgress.second,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        } else if (uiState.isLoading) {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        } else if (uiItems.isEmpty()) {
            EmptyScreen(Modifier.fillMaxSize())
        } else {
            LazyColumn(
                contentPadding = PaddingValues(all = dimensionResource(R.dimen.page_padding)),
                verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
            ) {
                items(uiItems, key = { it.id }) {
                    DatabaseR30ListItem(it, Modifier.animateItem())
                }
            }
        }
    }
}
