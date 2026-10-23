package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R

internal enum class FilterSizeClass { COMPACT, FULL }

internal enum class FilterPicker(
    val sizeClass: FilterSizeClass,
) {
    PACKS(FilterSizeClass.FULL),
    DIFFICULTIES(FilterSizeClass.COMPACT),
}

/**
 * The dialog's visible screen: the facets when no picker is open, otherwise the
 * picker on top of them.
 */
@Composable
internal fun FilterScreen(
    state: FilterDialogState,
    picker: FilterPicker?,
    fill: Boolean,
    onOpenPicker: (FilterPicker) -> Unit,
    onBack: () -> Unit,
    onDismiss: () -> Unit,
) {
    when (picker) {
        null -> {
            FacetsScreen(
                state = state,
                fill = fill,
                onOpenPicker = onOpenPicker,
                onDismiss = onDismiss,
            )
        }

        FilterPicker.PACKS -> {
            PackPickerScreen(state = state, fill = fill, onBack = onBack)
        }

        FilterPicker.DIFFICULTIES -> {
            DifficultyPickerScreen(state = state, fill = fill, onBack = onBack)
        }
    }
}

@Composable
private fun FacetsScreen(
    state: FilterDialogState,
    fill: Boolean,
    onOpenPicker: (FilterPicker) -> Unit,
    onDismiss: () -> Unit,
) {
    FilterDialogChrome(
        fill = fill,
        leading = {
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, stringResource(R.string.general_close))
            }
        },
        title = stringResource(R.string.database_play_result_filter_title),
        footer = {
            FilterDialogFooter(
                label =
                    pluralStringResource(
                        R.plurals.database_play_result_filter_result_count,
                        state.resultCount,
                        state.resultCount,
                    ),
                actionLabel = stringResource(R.string.general_reset),
                onAction = state::reset,
                onDone = onDismiss,
            )
        },
    ) {
        FacetsPage(
            state = state,
            onOpenPacks = { onOpenPicker(FilterPicker.PACKS) },
            onOpenRatingClasses = { onOpenPicker(FilterPicker.DIFFICULTIES) },
        )
    }
}

@Composable
private fun PackPickerScreen(
    state: FilterDialogState,
    fill: Boolean,
    onBack: () -> Unit,
) {
    FilterDialogChrome(
        fill = fill,
        leading = { BackButton(onBack) },
        title = stringResource(R.string.database_play_result_filter_pick_pack),
        footer = {
            FilterDialogFooter(
                label =
                    pluralStringResource(
                        R.plurals.database_play_result_filter_selected_packs,
                        state.filter.packIds.size,
                        state.filter.packIds.size,
                    ),
                actionLabel = stringResource(R.string.general_clear),
                onAction = { state.setPackIds(emptySet()) },
                onDone = onBack,
            )
        },
    ) {
        PackPickerPage(state = state)
    }
}

@Composable
private fun DifficultyPickerScreen(
    state: FilterDialogState,
    fill: Boolean,
    onBack: () -> Unit,
) {
    FilterDialogChrome(
        fill = fill,
        leading = { BackButton(onBack) },
        title = stringResource(R.string.database_play_result_filter_pick_difficulties),
        footer = {
            FilterDialogFooter(
                label = null,
                actionLabel = stringResource(R.string.general_clear),
                onAction = { state.setRatingClasses(emptySet()) },
                onDone = onBack,
            )
        },
    ) {
        DifficultyPickerPage(state = state)
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.icon_desc_navigate_back))
    }
}
