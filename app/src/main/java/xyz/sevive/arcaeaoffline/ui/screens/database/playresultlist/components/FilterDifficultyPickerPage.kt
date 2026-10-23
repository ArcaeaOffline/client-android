package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClassDisplay

@Composable
internal fun DifficultyPickerPage(
    state: FilterDialogState,
    modifier: Modifier = Modifier,
) {
    val selectedRatingClasses = state.filter.ratingClasses
    val ratingClassCounts = state.ratingClassCounts

    Column(
        modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .verticalScroll(rememberScrollState()),
    ) {
        ArcaeaRatingClass.entries.forEach { ratingClass ->
            val selected = ratingClass in selectedRatingClasses
            val count = ratingClassCounts[ratingClass] ?: 0

            SelectableRow(
                title = ArcaeaRatingClassDisplay.of(ratingClass).name,
                trailing =
                    pluralStringResource(
                        R.plurals.database_play_result_entries,
                        count,
                        count,
                    ),
                selected = selected,
                // A class without results under the other facets cannot be
                // added, but a selected one stays enabled so it can be removed.
                enabled = selected || count > 0,
                onClick = {
                    state.setRatingClasses(
                        if (selected) {
                            selectedRatingClasses - ratingClass
                        } else {
                            selectedRatingClasses + ratingClass
                        },
                    )
                },
            )
        }
    }
}
