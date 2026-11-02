package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.SubScreenContainer
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaSearchTextField
import xyz.sevive.arcaeaoffline.ui.components.LoadingOverlay
import xyz.sevive.arcaeaoffline.ui.navigation.DatabaseSubScreen
import xyz.sevive.arcaeaoffline.ui.navigation.LocalListDetailNavigationContext
import xyz.sevive.arcaeaoffline.ui.screens.EmptyScreen
import xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components.DatabasePlayResultFilterChips
import xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components.DatabasePlayResultFilterDialog
import xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components.DatabasePlayResultListAppBarActions
import xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components.FilterPicker
import xyz.sevive.arcaeaoffline.ui.theme.spacing
import xyz.sevive.arcaeaoffline.ui.utils.clearFocusOnKeyboardDismiss

@Composable
private fun DatabasePlayResultSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    ArcaeaSearchTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = stringResource(R.string.database_play_result_search_hint),
        modifier =
            modifier
                .padding(
                    start = MaterialTheme.spacing.pagePadding,
                    end = MaterialTheme.spacing.pagePadding,
                    top = MaterialTheme.spacing.sm,
                    bottom = MaterialTheme.spacing.sm,
                ).clearFocusOnKeyboardDismiss(),
    )
}

@Composable
fun DatabasePlayResultListScreen(viewModel: DatabasePlayResultListViewModel = koinViewModel()) {
    val context = LocalContext.current

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val catalog by viewModel.catalog.collectAsStateWithLifecycle()
    val selectedItemUuids by viewModel.selectedItemUuids.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val navContext = LocalListDetailNavigationContext.current

    val listItems = uiState.listItems

    val selectedItemsCount by remember { derivedStateOf { selectedItemUuids.size } }
    val visibleItemUuids by remember(listItems) { derivedStateOf { listItems.mapTo(HashSet()) { it.uuid } } }
    val hiddenSelectedCount by remember(selectedItemUuids, visibleItemUuids) {
        derivedStateOf { selectedItemUuids.count { it !in visibleItemUuids } }
    }

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
            hiddenSelectedCount = hiddenSelectedCount,
        )
    }

    var showSortDialog by rememberSaveable { mutableStateOf(false) }
    if (showSortDialog) {
        DatabasePlayResultListSortDialog(
            onDismissRequest = { showSortDialog = false },
            sortOrder = uiState.sortOrder,
            sortKey = uiState.sortKey,
            onSortOrderChange = { viewModel.setSortOrder(it) },
            onSortKeyChange = { viewModel.setSortKey(it) },
        )
    }

    var showFilterDialog by rememberSaveable { mutableStateOf(false) }
    var filterDialogPicker by rememberSaveable { mutableStateOf<FilterPicker?>(null) }
    if (showFilterDialog) {
        DatabasePlayResultFilterDialog(
            filter = uiState.filter,
            catalog = catalog,
            songPlayResultCounts = uiState.songPlayResultCounts,
            ratingClassCounts = uiState.ratingClassCounts,
            scoreGradeCounts = uiState.scoreGradeCounts,
            clearStatusCounts = uiState.clearStatusCounts,
            resultCount = listItems.size,
            initialPicker = filterDialogPicker,
            onFilterChange = viewModel::setFilter,
            onDismissRequest = { showFilterDialog = false },
        )
    }

    SubScreenContainer(
        title = stringResource(R.string.database_play_result_list_title),
        actions = {
            DatabasePlayResultListAppBarActions(
                inSelectMode = isInSelectMode,
                selectedItemsCount = selectedItemsCount,
                activeFilterCount = uiState.filter.activeCount,
                canSelectAll = listItems.isNotEmpty(),
                onShowFilterSheet = {
                    filterDialogPicker = null
                    showFilterDialog = true
                },
                onShowSortDialog = { showSortDialog = true },
                onShowDeleteConfirmDialog = { showDeleteConfirmDialog = true },
                onSelectAll = { viewModel.selectAllFiltered() },
                onSelectedModeChange = { isInSelectMode = it },
                onClearSelectedItems = { viewModel.clearSelectedItems() },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navContext.navigateToDetail(DatabaseSubScreen.AddPlayResult.route) }) {
                Icon(Icons.Default.Add, contentDescription = stringResource(DatabaseSubScreen.AddPlayResult.title))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) {
        // The adaptive pane scaffold moves focus onto the destination pane when it changes, and
        // the first focusable child of this pane is the search field, so the IME opens on entry
        // and after every configuration change. As a focus target, this column takes that focus
        // instead of the search field.
        // Upstream issue: https://issuetracker.google.com/issues/445720462
        // TODO: drop the focus target once adaptive-layout 1.3.0 is in use and
        //   PaneScaffoldDirective.shouldAutoFocusCurrentDestination is set to false.
        Column(
            Modifier
                .fillMaxSize()
                .focusTarget(),
        ) {
            DatabasePlayResultSearchBar(
                query = uiState.filter.query,
                onQueryChange = { viewModel.setQuery(it) },
            )

            // The row keeps the result count visible while the list scrolls.
            // Structured filter chips share the row and scroll horizontally
            // after it. An empty database has nothing to count, so the row is
            // hidden there.
            if (uiState.totalCount > 0) {
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = 32.dp)
                            .padding(horizontal = MaterialTheme.spacing.pagePadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                ) {
                    Text(
                        text =
                            if (uiState.filter.isActive) {
                                pluralStringResource(
                                    R.plurals.database_play_result_list_count,
                                    listItems.size,
                                    listItems.size,
                                    uiState.totalCount,
                                )
                            } else {
                                pluralStringResource(
                                    R.plurals.database_play_result_entries,
                                    uiState.totalCount,
                                    uiState.totalCount,
                                )
                            },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    if (uiState.filter.hasStructuredFilter) {
                        DatabasePlayResultFilterChips(
                            filter = uiState.filter,
                            catalog = catalog,
                            onOpenFilters = {
                                filterDialogPicker = it
                                showFilterDialog = true
                            },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            LoadingOverlay(
                loading = uiState.isLoading,
                Modifier.fillMaxSize(),
            ) {
                when {
                    uiState.totalCount == 0 -> {
                        EmptyScreen(Modifier.fillMaxSize())
                    }

                    listItems.isEmpty() -> {
                        EmptyScreen(
                            Modifier.fillMaxSize(),
                            text = stringResource(R.string.database_play_result_filter_no_match),
                            action = {
                                TextButton(onClick = { viewModel.clearFilters() }) {
                                    Text(stringResource(R.string.database_play_result_filter_clear))
                                }
                            },
                        )
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(all = MaterialTheme.spacing.pagePadding),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
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
    }
}
