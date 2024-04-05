package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreRatingClass
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme

enum class RatingClassSelectorStyle(val columns: Int) {
    Vertical(1), Grid(2), Horizontal(5),
}


@Composable
fun RatingClassSelector(
    ratingClass: ArcaeaScoreRatingClass?,
    onRatingClassChange: (ArcaeaScoreRatingClass) -> Unit,
    enabledRatingClasses: List<ArcaeaScoreRatingClass> = listOf(),
    style: RatingClassSelectorStyle = RatingClassSelectorStyle.Grid,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painterResource(R.drawable.ic_rating_class),
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LazyVerticalGrid(columns = GridCells.Fixed(style.columns)) {
            ArcaeaScoreRatingClass.entries.forEach {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = ratingClass == it,
                            onClick = { onRatingClassChange(it) },
                            enabled = enabledRatingClasses.contains(it),
                        )

                        Text(it.name)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun RatingClassSelectorPreview() {
    var selectedRatingClass by remember { mutableStateOf(ArcaeaScoreRatingClass.PAST) }

    val enabledRatingClasses = listOf(
        ArcaeaScoreRatingClass.PAST,
        ArcaeaScoreRatingClass.PRESENT,
        ArcaeaScoreRatingClass.FUTURE,
    )

    ArcaeaOfflineTheme {
        Surface {
            Column {
                Text("selected $selectedRatingClass")

                RatingClassSelector(
                    ratingClass = selectedRatingClass,
                    onRatingClassChange = { selectedRatingClass = it },
                    enabledRatingClasses = enabledRatingClasses
                )

                HorizontalDivider()

                RatingClassSelector(
                    ratingClass = selectedRatingClass,
                    onRatingClassChange = { selectedRatingClass = it },
                    enabledRatingClasses = enabledRatingClasses,
                    style = RatingClassSelectorStyle.Vertical,
                )

                HorizontalDivider()

                RatingClassSelector(
                    ratingClass = selectedRatingClass,
                    onRatingClassChange = { selectedRatingClass = it },
                    enabledRatingClasses = enabledRatingClasses,
                    style = RatingClassSelectorStyle.Horizontal,
                )
            }
        }
    }
}
