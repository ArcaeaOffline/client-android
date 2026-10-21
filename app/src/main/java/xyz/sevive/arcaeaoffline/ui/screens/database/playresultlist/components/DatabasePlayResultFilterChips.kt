package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalog
import xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.PlayResultFilter
import xyz.sevive.arcaeaoffline.ui.theme.spacing

/**
 * Read-only summary of the active filters. Tapping a chip opens the filter
 * dialog, which is the only place structured filters are added or removed.
 *
 * The row is laid out by the caller next to the result count. It carries no
 * horizontal content padding of its own.
 */
@Composable
fun DatabasePlayResultFilterChips(
    filter: PlayResultFilter,
    catalog: ArcaeaCatalog,
    onOpenFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!filter.hasStructuredFilter) return

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
    ) {
        if (filter.packIds.isNotEmpty()) {
            // Collapse the selection into one chip; a long pack selection would
            // otherwise push the other chips out of view.
            item(key = "packs") {
                AssistChip(
                    onClick = onOpenFilters,
                    label = { Text(packLabel(filter.packIds, catalog)) },
                    leadingIcon = {
                        Icon(
                            painterResource(R.drawable.ic_pack),
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize),
                        )
                    },
                )
            }
        }

        if (filter.onlyWarnings) {
            item(key = "warnings") {
                AssistChip(
                    onClick = onOpenFilters,
                    label = { Text(stringResource(R.string.database_play_result_filter_chip_warnings)) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            modifier = Modifier.size(AssistChipDefaults.IconSize),
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun packLabel(
    packIds: Set<String>,
    catalog: ArcaeaCatalog,
): String =
    if (packIds.size == 1) {
        stringResource(
            R.string.database_play_result_filter_chip_pack,
            catalog.packName(packIds.first()) ?: packIds.first(),
        )
    } else {
        stringResource(R.string.database_play_result_filter_chip_packs_count, packIds.size)
    }
