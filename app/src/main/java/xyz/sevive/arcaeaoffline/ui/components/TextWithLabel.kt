package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.TextStyle
import xyz.sevive.arcaeaoffline.R


@Composable
fun TextWithLabel(
    text: @Composable () -> Unit,
    label: @Composable (Color, TextStyle) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.text_label_padding)),
    ) {
        label(MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.typography.labelMedium)
        text()
    }
}

@Composable
fun TextWithLabel(text: String, label: String, modifier: Modifier = Modifier) {
    TextWithLabel(
        text = { Text(text) },
        label = { color, textStyle ->
            Text(label, color = color, style = textStyle)
        },
        modifier = modifier,
    )
}
