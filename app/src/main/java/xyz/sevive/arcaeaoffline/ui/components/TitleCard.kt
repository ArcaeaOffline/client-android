package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import xyz.sevive.arcaeaoffline.R

@Composable
fun TitleOutlinedCard(
    title: @Composable (paddingValues: PaddingValues) -> Unit,
    modifier: Modifier = Modifier,
    customPaddingValues: PaddingValues? = null,
    titleContainerColor: Color = MaterialTheme.colorScheme.surface,
    titleContentColor: Color = MaterialTheme.colorScheme.onSurface,
    content: @Composable ColumnScope.(paddingValues: PaddingValues) -> Unit,
) {
    val paddingValues = customPaddingValues ?: PaddingValues(
        dimensionResource(R.dimen.general_card_padding)
    )

    OutlinedCard(modifier) {
        Surface(color = titleContainerColor, contentColor = titleContentColor) {
            title(paddingValues)
        }

        content(paddingValues)
    }
}
