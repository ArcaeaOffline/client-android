package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearStatus
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClassDisplay
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaScoreGrade
import xyz.sevive.arcaeaoffline.core.search.ArcaeaCatalog
import xyz.sevive.arcaeaoffline.ui.components.SwitchItem
import xyz.sevive.arcaeaoffline.ui.components.TextItem

@Composable
internal fun FacetsPage(
    state: FilterDialogState,
    onOpenPicker: (FilterPicker) -> Unit,
    modifier: Modifier = Modifier,
) {
    val filter = state.filter
    val catalog = state.catalog
    val onFilterChange = state.onFilterChange

    Column(
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(rememberScrollState()),
    ) {
        FilterEntryRow(
            label = stringResource(R.string.database_play_result_filter_packs),
            value = packSummary(filter.packIds, catalog),
            leading = { Icon(painterResource(R.drawable.ic_pack), contentDescription = null) },
            onClick = { onOpenPicker(FilterPicker.PACKS) },
            onClear =
                if (filter.packIds.isEmpty()) {
                    null
                } else {
                    { onFilterChange(filter.copy(packIds = emptySet())) }
                },
        )

        FilterEntryRow(
            label = stringResource(R.string.database_play_result_filter_difficulty),
            value = ratingClassSummary(filter.ratingClasses),
            leading = { Icon(painterResource(R.drawable.ic_rating_class), contentDescription = null) },
            onClick = { onOpenPicker(FilterPicker.DIFFICULTIES) },
            onClear =
                if (filter.ratingClasses.isEmpty()) {
                    null
                } else {
                    { onFilterChange(filter.copy(ratingClasses = emptySet())) }
                },
        )

        FilterEntryRow(
            label = stringResource(R.string.database_play_result_filter_score),
            value = scoreGradesSummary(filter.scoreGrades),
            onClick = { onOpenPicker(FilterPicker.SCORE_GRADES) },
            onClear =
                if (filter.scoreGrades.isEmpty()) {
                    null
                } else {
                    { onFilterChange(filter.copy(scoreGrades = emptySet())) }
                },
        )

        FilterEntryRow(
            label = stringResource(R.string.database_play_result_filter_clear_status),
            value = clearStatusSummary(filter.clearStatuses),
            onClick = { onOpenPicker(FilterPicker.CLEAR_STATUSES) },
            onClear =
                if (filter.clearStatuses.isEmpty()) {
                    null
                } else {
                    { onFilterChange(filter.copy(clearStatuses = emptySet())) }
                },
        )

        SwitchItem(
            value = filter.onlyWarnings,
            onValueChange = { onFilterChange(filter.copy(onlyWarnings = it)) },
            title = stringResource(R.string.database_play_result_filter_only_warnings),
            icon = Icons.Default.Warning,
            description = stringResource(R.string.database_play_result_filter_only_warnings_description),
        )
    }
}

@Composable
private fun FilterEntryRow(
    label: String,
    value: String,
    onClick: () -> Unit,
    onClear: (() -> Unit)?,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    TextItem(
        title = label,
        modifier = modifier,
        content = value,
        contentMaxLines = 1,
        // Rows without an icon keep the same text inset as rows with one.
        leadingSlot = leading ?: { Spacer(Modifier.size(24.dp)) },
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
        onClick = onClick,
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

@Composable
private fun scoreGradesSummary(scoreGrades: Set<ArcaeaScoreGrade>): String =
    when {
        scoreGrades.isEmpty() -> {
            stringResource(R.string.database_play_result_filter_all_scores)
        }

        scoreGrades.size == 1 -> {
            scoreGrades.first().toDisplayString()
        }

        else -> {
            stringResource(R.string.database_play_result_filter_selected_count, scoreGrades.size)
        }
    }

@Composable
private fun clearStatusSummary(clearStatuses: Set<ArcaeaPlayResultClearStatus>): String =
    when {
        clearStatuses.isEmpty() -> {
            stringResource(R.string.database_play_result_filter_all_clear_statuses)
        }

        clearStatuses.size == 1 -> {
            stringResource(clearStatusLabelRes(clearStatuses.first()))
        }

        else -> {
            stringResource(R.string.database_play_result_filter_selected_count, clearStatuses.size)
        }
    }
