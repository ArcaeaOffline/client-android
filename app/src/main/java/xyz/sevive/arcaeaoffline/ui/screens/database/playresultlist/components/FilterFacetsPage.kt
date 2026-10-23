package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClassDisplay
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalog
import xyz.sevive.arcaeaoffline.ui.components.BaseSettingsItem
import xyz.sevive.arcaeaoffline.ui.components.SwitchItem
import xyz.sevive.arcaeaoffline.ui.theme.spacing

@Composable
internal fun FacetsPage(
    state: FilterDialogState,
    onOpenPacks: () -> Unit,
    onOpenRatingClasses: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val filter = state.filter
    val catalog = state.catalog
    val onFilterChange = state.onFilterChange

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

        FilterEntryRow(
            label = stringResource(R.string.database_play_result_filter_difficulty),
            value = ratingClassSummary(filter.ratingClasses),
            leading = { Icon(painterResource(R.drawable.ic_rating_class), contentDescription = null) },
            onClick = onOpenRatingClasses,
            onClear =
                if (filter.ratingClasses.isEmpty()) {
                    null
                } else {
                    { onFilterChange(filter.copy(ratingClasses = emptySet())) }
                },
            enabled = true,
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

@Composable
private fun ratingClassSummary(ratingClasses: Set<ArcaeaRatingClass>): String =
    when {
        ratingClasses.isEmpty() -> {
            stringResource(R.string.database_play_result_filter_all_difficulties)
        }

        ratingClasses.size == 1 -> {
            ArcaeaRatingClassDisplay.of(ratingClasses.first()).name
        }

        else -> {
            stringResource(R.string.database_play_result_filter_selected_count, ratingClasses.size)
        }
    }
