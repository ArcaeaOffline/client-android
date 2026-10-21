package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Pack
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalog
import xyz.sevive.arcaeaoffline.core.search.SearchEngine
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaSearchTextField
import xyz.sevive.arcaeaoffline.ui.components.BaseSettingsItem
import xyz.sevive.arcaeaoffline.ui.components.SwitchItem
import xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.PlayResultFilter
import xyz.sevive.arcaeaoffline.ui.theme.spacing

private enum class FilterPage { FACETS, PACKS }

private val FilterDialogMaxWidth = 640.dp

private const val PAGE_TRANSITION_DURATION_MILLIS = 220

/**
 * Full-screen filter dialog.
 *
 * Filters apply live. [onDismissRequest] is also used for back: on the pack page
 * it returns to the facets instead of closing the dialog.
 */
@Composable
fun DatabasePlayResultFilterDialog(
    filter: PlayResultFilter,
    catalog: ArcaeaCatalog,
    songPlayResultCounts: Map<String, Int>,
    resultCount: Int,
    onFilterChange: (PlayResultFilter) -> Unit,
    onPackIdsChange: (Set<String>) -> Unit,
    onDismissRequest: () -> Unit,
) {
    var page by rememberSaveable { mutableStateOf(FilterPage.FACETS) }

    fun handleBack() {
        if (page == FilterPage.FACETS) onDismissRequest() else page = FilterPage.FACETS
    }

    Dialog(
        onDismissRequest = { handleBack() },
        properties =
            DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false,
            ),
    ) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                Column(
                    Modifier
                        .fillMaxHeight()
                        .widthIn(max = FilterDialogMaxWidth)
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.safeDrawing),
                ) {
                    FilterDialogHeader(
                        page = page,
                        selectedPackIds = filter.packIds,
                        onBack = { handleBack() },
                        onClearPacks = { onPackIdsChange(emptySet()) },
                    )

                    Box(Modifier.weight(1f).fillMaxWidth()) {
                        AnimatedContent(
                            targetState = page,
                            modifier = Modifier.fillMaxSize(),
                            transitionSpec = {
                                // The content area has a fixed height, so only the
                                // horizontal slide changes between pages.
                                val forward = targetState.ordinal > initialState.ordinal
                                val enter =
                                    slideInHorizontally(animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS)) { width ->
                                        if (forward) width else -width
                                    }
                                val exit =
                                    slideOutHorizontally(animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS)) { width ->
                                        if (forward) -width else width
                                    }
                                enter togetherWith exit
                            },
                            label = "filterPage",
                        ) { currentPage ->
                            when (currentPage) {
                                FilterPage.FACETS -> {
                                    FacetsPage(
                                        filter = filter,
                                        catalog = catalog,
                                        onFilterChange = onFilterChange,
                                        onOpenPacks = { page = FilterPage.PACKS },
                                    )
                                }

                                FilterPage.PACKS -> {
                                    PackPickerPage(
                                        catalog = catalog,
                                        selectedPackIds = filter.packIds,
                                        songPlayResultCounts = songPlayResultCounts,
                                        onPackIdsChange = onPackIdsChange,
                                    )
                                }
                            }
                        }
                    }

                    FilterDialogFooter(
                        resultCount = resultCount,
                        onReset = { onFilterChange(PlayResultFilter(query = filter.query)) },
                        onDone = onDismissRequest,
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterDialogHeader(
    page: FilterPage,
    selectedPackIds: Set<String>,
    onBack: () -> Unit,
    onClearPacks: () -> Unit,
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.xs, vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            if (page == FilterPage.FACETS) {
                Icon(Icons.Default.Close, stringResource(R.string.general_close))
            } else {
                Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.icon_desc_navigate_back))
            }
        }

        Text(
            text =
                when (page) {
                    FilterPage.FACETS -> stringResource(R.string.database_play_result_filter_title)
                    FilterPage.PACKS -> stringResource(R.string.database_play_result_filter_pick_pack)
                },
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(Modifier.weight(1f))

        if (page == FilterPage.PACKS) {
            Text(
                text = stringResource(R.string.database_play_result_filter_selected_count, selectedPackIds.size),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.width(MaterialTheme.spacing.xs))

            AnimatedVisibility(
                visible = selectedPackIds.isNotEmpty(),
                enter = fadeIn() + expandHorizontally(),
                exit = fadeOut() + shrinkHorizontally(),
            ) {
                TextButton(onClick = onClearPacks) {
                    Text(stringResource(R.string.general_clear))
                }
            }
        }
    }
}

@Composable
private fun FacetsPage(
    filter: PlayResultFilter,
    catalog: ArcaeaCatalog,
    onFilterChange: (PlayResultFilter) -> Unit,
    onOpenPacks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.pagePadding),
    ) {
        Spacer(Modifier.height(MaterialTheme.spacing.lg))

        FilterEntryRow(
            label = stringResource(R.string.database_play_result_filter_packs),
            value = packSummary(filter.packIds, catalog),
            leading = { Icon(painterResource(R.drawable.ic_pack), contentDescription = null) },
            onClick = onOpenPacks,
            onClear =
                if (filter.packIds.isEmpty()) {
                    null
                } else {
                    { onFilterChange(filter.copy(packIds = emptySet())) }
                },
            enabled = catalog.basePacks.isNotEmpty(),
        )

        SwitchItem(
            value = filter.onlyWarnings,
            onValueChange = { onFilterChange(filter.copy(onlyWarnings = it)) },
            title = stringResource(R.string.database_play_result_filter_only_warnings),
            icon = Icons.Default.Warning,
            description = stringResource(R.string.database_play_result_filter_only_warnings_description),
        )

        if (catalog.isLoaded && catalog.songs.isEmpty()) {
            Text(
                text = stringResource(R.string.database_play_result_filter_no_song_data),
                modifier = Modifier.padding(top = MaterialTheme.spacing.sm),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FilterEntryRow(
    label: String,
    value: String,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
    onClear: (() -> Unit)?,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    BaseSettingsItem(
        title = { Text(label) },
        content = {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
        modifier = modifier.alpha(if (enabled) 1f else 0.5f),
        onClick = if (enabled) onClick else null,
        leadingSlot = leading,
        trailingSlot = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onClear != null) {
                    IconButton(onClick = onClear) {
                        Icon(Icons.Default.Close, contentDescription = null)
                    }
                }
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        },
    )
}

@Composable
private fun packSummary(
    packIds: Set<String>,
    catalog: ArcaeaCatalog,
): String =
    when {
        packIds.isEmpty() -> stringResource(R.string.database_play_result_filter_all_packs)
        packIds.size == 1 -> catalog.packName(packIds.first()) ?: packIds.first()
        else -> stringResource(R.string.database_play_result_filter_selected_count, packIds.size)
    }

private data class PackRowData(
    val pack: Pack,
    val appendNames: List<String>,
    val searchableText: String,
    val playResultCount: Int,
)

@Composable
private fun PackPickerPage(
    catalog: ArcaeaCatalog,
    selectedPackIds: Set<String>,
    songPlayResultCounts: Map<String, Int>,
    onPackIdsChange: (Set<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var showEmptyPacks by rememberSaveable { mutableStateOf(songPlayResultCounts.isEmpty()) }

    val rows =
        remember(catalog, songPlayResultCounts) {
            catalog.basePacks
                .map { pack ->
                    val appendNames = catalog.appendPacksByBasePackId[pack.id].orEmpty().map { it.name }
                    PackRowData(
                        pack = pack,
                        appendNames = appendNames,
                        searchableText =
                            SearchEngine.normalize(
                                buildString {
                                    append(pack.name)
                                    append(' ')
                                    append(pack.id)
                                    appendNames.forEach {
                                        append(' ')
                                        append(it)
                                    }
                                },
                            ),
                        playResultCount = catalog.songIdsByBasePackId[pack.id].orEmpty().sumOf { songPlayResultCounts[it] ?: 0 },
                    )
                }.sortedByDescending { it.playResultCount }
        }

    val normalizedQuery = SearchEngine.normalize(query)
    val visibleRows =
        rows.filter { row ->
            (showEmptyPacks || row.playResultCount > 0) &&
                (normalizedQuery.isEmpty() || row.searchableText.contains(normalizedQuery))
        }

    Column(modifier.fillMaxSize()) {
        ArcaeaSearchTextField(
            value = query,
            onValueChange = { query = it },
            placeholder = stringResource(R.string.database_play_result_filter_search_pack_hint),
            modifier =
                Modifier.padding(
                    horizontal = MaterialTheme.spacing.pagePadding,
                    vertical = MaterialTheme.spacing.sm,
                ),
        )

        SwitchItem(
            value = showEmptyPacks,
            onValueChange = { showEmptyPacks = it },
            title = { Text(stringResource(R.string.database_play_result_filter_show_empty_packs)) },
        )

        LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
            items(visibleRows, key = { it.pack.id }) { row ->
                SelectableRow(
                    title = row.pack.name,
                    subtitle =
                        row.appendNames
                            .takeIf { it.isNotEmpty() }
                            ?.let {
                                stringResource(
                                    R.string.database_play_result_filter_append_packs,
                                    it.joinToString(),
                                )
                            },
                    trailing =
                        pluralStringResource(
                            R.plurals.database_play_result_entries,
                            row.playResultCount,
                            row.playResultCount,
                        ),
                    selected = row.pack.id in selectedPackIds,
                    onClick = {
                        onPackIdsChange(
                            if (row.pack.id in selectedPackIds) {
                                selectedPackIds - row.pack.id
                            } else {
                                selectedPackIds + row.pack.id
                            },
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun SelectableRow(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    trailing: String? = null,
) {
    Row(
        modifier
            .fillMaxWidth()
            .toggleable(
                value = selected,
                role = Role.Checkbox,
                onValueChange = { onClick() },
            ).padding(horizontal = MaterialTheme.spacing.pagePadding, vertical = MaterialTheme.spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(checked = selected, onCheckedChange = null)

        Column(Modifier.weight(1f).padding(start = MaterialTheme.spacing.sm)) {
            Text(title, style = MaterialTheme.typography.titleSmall)

            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FilterDialogFooter(
    resultCount: Int,
    onReset: () -> Unit,
    onDone: () -> Unit,
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.pagePadding, vertical = MaterialTheme.spacing.sm),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text =
                    pluralStringResource(
                        R.plurals.database_play_result_filter_result_count,
                        resultCount,
                        resultCount,
                    ),
                style = MaterialTheme.typography.bodyMedium,
            )

            Spacer(Modifier.weight(1f))

            TextButton(onClick = onReset) {
                Text(stringResource(R.string.general_reset))
            }

            Button(onClick = onDone) {
                Text(stringResource(R.string.general_action_done))
            }
        }
    }
}
