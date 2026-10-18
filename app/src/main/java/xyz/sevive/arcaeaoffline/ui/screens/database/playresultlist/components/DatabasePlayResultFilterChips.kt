package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
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
 * Read-only summary of the active filters. Tapping the chip opens the filter
 * dialog, which is the only place filters are added or removed.
 *
 * The pack selection collapses into one chip with a count (e.g. "Packs · 3") so
 * a long selection cannot push everything else out of view.
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
