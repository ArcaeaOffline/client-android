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
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearStatus
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClassDisplay
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreGrade

internal enum class FilterSizeClass { COMPACT, FULL }

internal enum class FilterPicker(
    val sizeClass: FilterSizeClass,
) {
    PACKS(FilterSizeClass.FULL),
    DIFFICULTIES(FilterSizeClass.COMPACT),
    SCORE_GRADES(FilterSizeClass.COMPACT),
    CLEAR_STATUSES(FilterSizeClass.COMPACT),
}

/** Renders the facets, or the picker opened over them. */
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

        FilterPicker.SCORE_GRADES -> {
            ScoreGradePickerScreen(state = state, fill = fill, onBack = onBack)
        }

        FilterPicker.CLEAR_STATUSES -> {
            ClearStatusPickerScreen(state = state, fill = fill, onBack = onBack)
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
        FacetsPage(state = state, onOpenPicker = onOpenPicker)
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
        FilterOptionPickerPage(
            options = ArcaeaRatingClass.entries,
            selected = state.filter.ratingClasses,
            counts = state.ratingClassCounts,
            label = { ArcaeaRatingClassDisplay.of(it).name },
            onSelectionChange = state::setRatingClasses,
        )
    }
}

@Composable
private fun ScoreGradePickerScreen(
    state: FilterDialogState,
    fill: Boolean,
    onBack: () -> Unit,
) {
    FilterDialogChrome(
        fill = fill,
        leading = { BackButton(onBack) },
        title = stringResource(R.string.database_play_result_filter_pick_score_grades),
        footer = {
            FilterDialogFooter(
                label = null,
                actionLabel = stringResource(R.string.general_clear),
                onAction = { state.setScoreGrades(emptySet()) },
                onDone = onBack,
            )
        },
    ) {
        FilterOptionPickerPage(
            options = ArcaeaScoreGrade.entries,
            selected = state.filter.scoreGrades,
            counts = state.scoreGradeCounts,
            label = { it.toDisplayString() },
            onSelectionChange = state::setScoreGrades,
        )
    }
}

@Composable
private fun ClearStatusPickerScreen(
    state: FilterDialogState,
    fill: Boolean,
    onBack: () -> Unit,
) {
    FilterDialogChrome(
        fill = fill,
        leading = { BackButton(onBack) },
        title = stringResource(R.string.database_play_result_filter_pick_clear_statuses),
        footer = {
            FilterDialogFooter(
                label = null,
                actionLabel = stringResource(R.string.general_clear),
                onAction = { state.setClearStatuses(emptySet()) },
                onDone = onBack,
            )
        },
    ) {
        FilterOptionPickerPage(
            options = ArcaeaPlayResultClearStatus.entries,
            selected = state.filter.clearStatuses,
            counts = state.clearStatusCounts,
            label = { stringResource(clearStatusLabelRes(it)) },
            onSelectionChange = state::setClearStatuses,
        )
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack) {
        Icon(Icons.AutoMirrored.Default.ArrowBack, stringResource(R.string.icon_desc_navigate_back))
    }
}

internal fun clearStatusLabelRes(status: ArcaeaPlayResultClearStatus): Int =
    when (status) {
        ArcaeaPlayResultClearStatus.TRACK_LOST -> R.string.database_play_result_filter_clear_status_track_lost
        ArcaeaPlayResultClearStatus.CLEARED -> R.string.database_play_result_filter_clear_status_cleared
        ArcaeaPlayResultClearStatus.FULL_RECALL -> R.string.database_play_result_filter_clear_status_full_recall
        ArcaeaPlayResultClearStatus.PURE_MEMORY -> R.string.database_play_result_filter_clear_status_pure_memory
    }
