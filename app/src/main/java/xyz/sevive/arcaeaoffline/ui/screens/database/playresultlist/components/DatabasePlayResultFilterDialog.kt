package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
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

private val FilterDialogCompactMaxWidth = 480.dp

private const val FILTER_DIALOG_COMPACT_WIDTH_FRACTION = 0.92f

private val FilterDialogCompactCorner = 28.dp

private const val PAGE_TRANSITION_DURATION_MILLIS = 220

/**
 * Filter dialog: a compact facet card that expands into a full-screen pack
 * picker.
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

    val onReset: () -> Unit = { onFilterChange(PlayResultFilter(query = filter.query)) }
    val onClearPacks: () -> Unit = { onPackIdsChange(emptySet()) }

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
        BoxWithConstraints(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            val availableWidth = maxWidth
            val availableHeight = maxHeight
            val compactWidth =
                minOf(availableWidth * FILTER_DIALOG_COMPACT_WIDTH_FRACTION, FilterDialogCompactMaxWidth)
            val density = LocalDensity.current
            val safeInsets = WindowInsets.safeDrawing.asPaddingValues()
            val layoutDirection = LocalLayoutDirection.current

            val startInset by animateDpAsState(
                targetValue = if (page == FilterPage.PACKS) safeInsets.calculateStartPadding(layoutDirection) else 0.dp,
                animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS),
                label = "filterStartInset",
            )
            val topInset by animateDpAsState(
                targetValue = if (page == FilterPage.PACKS) safeInsets.calculateTopPadding() else 0.dp,
                animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS),
                label = "filterTopInset",
            )
            val endInset by animateDpAsState(
                targetValue = if (page == FilterPage.PACKS) safeInsets.calculateEndPadding(layoutDirection) else 0.dp,
                animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS),
                label = "filterEndInset",
            )
            val bottomInset by animateDpAsState(
                targetValue = if (page == FilterPage.PACKS) safeInsets.calculateBottomPadding() else 0.dp,
                animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS),
                label = "filterBottomInset",
            )

            // Measure the compact card once so page changes can animate between
            // its concrete size and the full available size.
            var compactSize by remember { mutableStateOf<IntSize?>(null) }

            val compactHeight = compactSize?.let { with(density) { it.height.toDp() } } ?: 0.dp

            // Seed the animations with the measured size so the first frame after
            // measuring does not animate in from zero.
            val animatedWidth =
                remember(compactSize) {
                    Animatable(compactSize?.let { with(density) { it.width.toDp().value } } ?: 0f)
                }
            val animatedHeight =
                remember(compactSize) {
                    Animatable(compactSize?.let { with(density) { it.height.toDp().value } } ?: 0f)
                }

            LaunchedEffect(page, compactSize, availableWidth, availableHeight) {
                if (compactSize == null) return@LaunchedEffect

                val targetWidth = if (page == FilterPage.PACKS) availableWidth.value else compactWidth.value
                val targetHeight = if (page == FilterPage.PACKS) availableHeight.value else compactHeight.value

                launch { animatedWidth.animateTo(targetWidth, tween(PAGE_TRANSITION_DURATION_MILLIS)) }
                launch { animatedHeight.animateTo(targetHeight, tween(PAGE_TRANSITION_DURATION_MILLIS)) }
            }

            val corner by animateDpAsState(
                targetValue = if (page == FilterPage.PACKS) 0.dp else FilterDialogCompactCorner,
                animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS),
                label = "filterCorner",
            )
            val elevation by animateDpAsState(
                targetValue = if (page == FilterPage.PACKS) 0.dp else 6.dp,
                animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS),
                label = "filterElevation",
            )

            val measuring = page == FilterPage.FACETS && compactSize == null
            val sizeModifier =
                when {
                    measuring -> Modifier.width(compactWidth).wrapContentHeight()
                    compactSize == null -> Modifier.fillMaxSize()
                    else -> Modifier.width(animatedWidth.value.dp).height(animatedHeight.value.dp)
                }

            Surface(
                modifier =
                    sizeModifier.onSizeChanged {
                        if (measuring) compactSize = it
                    },
                shape = RoundedCornerShape(corner),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = elevation,
                shadowElevation = elevation,
            ) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .then(if (measuring) Modifier else Modifier.fillMaxSize())
                        .padding(start = startInset, top = topInset, end = endInset, bottom = bottomInset),
                ) {
                    if (measuring) {
                        FilterDialogPage(
                            page = FilterPage.FACETS,
                            fill = false,
                            filter = filter,
                            catalog = catalog,
                            songPlayResultCounts = songPlayResultCounts,
                            resultCount = resultCount,
                            onFilterChange = onFilterChange,
                            onPackIdsChange = onPackIdsChange,
                            onBack = { handleBack() },
                            onClearPacks = onClearPacks,
                            onReset = onReset,
                            onDismissRequest = onDismissRequest,
                            onOpenPacks = { page = FilterPage.PACKS },
                        )
                    } else {
                        Crossfade(
                            targetState = page,
                            animationSpec = tween(PAGE_TRANSITION_DURATION_MILLIS),
                            label = "filterPage",
                        ) { currentPage ->
                            FilterDialogPage(
                                page = currentPage,
                                fill = true,
                                filter = filter,
                                catalog = catalog,
                                songPlayResultCounts = songPlayResultCounts,
                                resultCount = resultCount,
                                onFilterChange = onFilterChange,
                                onPackIdsChange = onPackIdsChange,
                                onBack = { handleBack() },
                                onClearPacks = onClearPacks,
                                onReset = onReset,
                                onDismissRequest = onDismissRequest,
                                onOpenPacks = { page = FilterPage.PACKS },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterDialogPage(
    page: FilterPage,
    fill: Boolean,
    filter: PlayResultFilter,
    catalog: ArcaeaCatalog,
    songPlayResultCounts: Map<String, Int>,
    resultCount: Int,
    onFilterChange: (PlayResultFilter) -> Unit,
    onPackIdsChange: (Set<String>) -> Unit,
    onBack: () -> Unit,
    onClearPacks: () -> Unit,
    onReset: () -> Unit,
    onDismissRequest: () -> Unit,
    onOpenPacks: () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .then(if (fill) Modifier.fillMaxSize() else Modifier),
    ) {
        FilterDialogHeader(page = page, onBack = onBack)

        val body: @Composable () -> Unit = {
            FilterDialogBody(
                page = page,
                filter = filter,
                catalog = catalog,
                songPlayResultCounts = songPlayResultCounts,
                onFilterChange = onFilterChange,
                onPackIdsChange = onPackIdsChange,
                onOpenPacks = onOpenPacks,
            )
        }

        if (fill) {
            Box(Modifier.weight(1f).fillMaxWidth()) { body() }
        } else {
            body()
        }

        FilterDialogFooter(
            page = page,
            selectedPackCount = filter.packIds.size,
            resultCount = resultCount,
            onClearPacks = onClearPacks,
            onReset = onReset,
            onDone = if (page == FilterPage.PACKS) onBack else onDismissRequest,
        )
    }
}

@Composable
private fun FilterDialogBody(
    page: FilterPage,
    filter: PlayResultFilter,
    catalog: ArcaeaCatalog,
    songPlayResultCounts: Map<String, Int>,
    onFilterChange: (PlayResultFilter) -> Unit,
    onPackIdsChange: (Set<String>) -> Unit,
    onOpenPacks: () -> Unit,
) {
    when (page) {
        FilterPage.FACETS -> {
            FacetsPage(
                filter = filter,
                catalog = catalog,
                onFilterChange = onFilterChange,
                onOpenPacks = onOpenPacks,
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

@Composable
private fun FilterDialogHeader(
    page: FilterPage,
    onBack: () -> Unit,
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
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
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
            .fillMaxWidth()
            .wrapContentHeight()
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
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
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
        packIds.isEmpty() -> {
            stringResource(R.string.database_play_result_filter_all_packs)
        }

        packIds.size == 1 -> {
            catalog.packName(packIds.first()) ?: packIds.first()
        }

        else -> {
            stringResource(R.string.database_play_result_filter_selected_count, packIds.size)
        }
    }

private data class PackRowData(
    val pack: Pack,
    val appendNames: List<String>,
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
                    PackRowData(
                        pack = pack,
                        appendNames = catalog.appendPacksByBasePackId[pack.id].orEmpty().map { it.name },
                        playResultCount = catalog.songIdsByBasePackId[pack.id].orEmpty().sumOf { songPlayResultCounts[it] ?: 0 },
                    )
                }.sortedByDescending { it.playResultCount }
        }

    val visibleRows =
        remember(rows, query, showEmptyPacks, catalog.packDocuments) {
            val matchedPackIds =
                if (query.isBlank()) null else SearchEngine.searchIds(catalog.packDocuments, query)

            rows.filter { row ->
                (showEmptyPacks || row.playResultCount > 0) &&
                    (matchedPackIds == null || row.pack.id in matchedPackIds)
            }
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
    page: FilterPage,
    selectedPackCount: Int,
    resultCount: Int,
    onClearPacks: () -> Unit,
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
                    if (page == FilterPage.PACKS) {
                        pluralStringResource(
                            R.plurals.database_play_result_filter_selected_packs,
                            selectedPackCount,
                            selectedPackCount,
                        )
                    } else {
                        pluralStringResource(
                            R.plurals.database_play_result_filter_result_count,
                            resultCount,
                            resultCount,
                        )
                    },
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            TextButton(
                onClick = if (page == FilterPage.PACKS) onClearPacks else onReset,
            ) {
                Text(
                    stringResource(
                        if (page == FilterPage.PACKS) R.string.general_clear else R.string.general_reset,
                    ),
                )
            }

            Button(onClick = onDone) {
                Text(stringResource(R.string.general_action_done))
            }
        }
    }
}
