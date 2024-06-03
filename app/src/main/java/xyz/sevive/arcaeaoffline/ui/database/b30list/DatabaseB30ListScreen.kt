package xyz.sevive.arcaeaoffline.ui.database.b30list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.AppViewModelProvider
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.SubScreenTopAppBar
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseScreenDestinations
import xyz.sevive.arcaeaoffline.ui.screens.EmptyScreen
import kotlin.math.round


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseB30ListScreen(
    onNavigateUp: () -> Unit,
    viewModel: DatabaseB30ListViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val coroutineScope = rememberCoroutineScope()

    var showOptions by rememberSaveable { mutableStateOf(false) }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val limit by viewModel.limit.collectAsStateWithLifecycle()

    val uiItems = uiState.uiItems

    SubScreenContainer(
        topBar = {
            SubScreenTopAppBar(
                onNavigateUp = onNavigateUp,
                title = { Text(stringResource(DatabaseScreenDestinations.B30.title)) },
                actions = {
                    IconButton(onClick = { coroutineScope.launch { viewModel.setLimit(limit) } }) {
                        Icon(Icons.Default.Refresh, null)
                    }
                    IconButton(onClick = { showOptions = true }) {
                        Icon(Icons.Default.Settings, null)
                    }
                },
            )
        },
    ) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize()) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            }
        } else if (uiItems.isEmpty()) {
            EmptyScreen(Modifier.fillMaxSize())
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))) {
                items(uiItems, key = { it.id }) {
                    DatabaseB30ListItem(it, Modifier.animateItem())
                }
            }
        }
    }

    if (showOptions) {
        AlertDialog(
            onDismissRequest = { showOptions = false },
            confirmButton = {},
            title = { Text(limit.toString()) },
            text = {
                Slider(
                    value = limit.toFloat(),
                    onValueChange = {
                        coroutineScope.launch { viewModel.setLimit(round(it).toInt()) }
                    },
                    valueRange = 10.0f..60.0f,
                    steps = 4,
                )
            },
        )
    }
}
