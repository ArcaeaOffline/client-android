package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import xyz.sevive.arcaeaoffline.R


data class TitleOutlinedCardColors(
    val titleContainerColor: Color,
    val titleContentColor: Color,
    val contentContainerColor: Color,
    val contentColor: Color,
)

object TitleOutlinedCardDefaults {
    @Composable
    fun paddingValues(): PaddingValues {
        return PaddingValues(dimensionResource(R.dimen.card_padding))
    }

    @Composable
    fun colors(
        titleContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
        titleContentColor: Color = MaterialTheme.colorScheme.primary,
        contentContainerColor: Color = Color.Transparent,
        contentColor: Color = contentColorFor(contentContainerColor),
    ): TitleOutlinedCardColors {
        return TitleOutlinedCardColors(
            titleContainerColor,
            titleContentColor,
            contentContainerColor,
            contentColor,
        )
    }
}

@Composable
fun TitleOutlinedCard(
    title: @Composable ColumnScope.(paddingValues: PaddingValues) -> Unit,
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = TitleOutlinedCardDefaults.paddingValues(),
    colors: TitleOutlinedCardColors = TitleOutlinedCardDefaults.colors(),
    content: @Composable ColumnScope.(paddingValues: PaddingValues) -> Unit,
) {
    OutlinedCard(modifier) {
        Surface(
            Modifier.fillMaxWidth(),
            color = colors.titleContainerColor,
            contentColor = colors.titleContentColor
        ) {
            title(paddingValues)
        }

        Surface(
            Modifier.fillMaxWidth(),
            color = colors.contentContainerColor,
            contentColor = colors.contentColor
        ) {
            content(paddingValues)
        }
    }
}

