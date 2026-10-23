package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.Pack
import xyz.sevive.arcaeaoffline.core.search.SearchEngine
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaSearchTextField
import xyz.sevive.arcaeaoffline.ui.components.SwitchItem
import xyz.sevive.arcaeaoffline.ui.theme.spacing

private data class PackRowData(
    val pack: Pack,
    val appendNames: List<String>,
    val playResultCount: Int,
)

@Composable
internal fun PackPickerPage(
    state: FilterDialogState,
    modifier: Modifier = Modifier,
) {
    val catalog = state.catalog
    val selectedPackIds = state.filter.packIds
    val songPlayResultCounts = state.songPlayResultCounts

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
                        state.setPackIds(
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
