package xyz.sevive.arcaeaoffline.ui.components.preferences

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha


@Composable
fun TextPreferencesWidget(
    title: String,
    modifier: Modifier = Modifier,
    content: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    trailingIcon: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
) {
    BasePreferencesWidget(
        title = { Text(title) },
        content = content?.let {
            { Text(it, Modifier.secondaryItemAlpha(), style = MaterialTheme.typography.bodySmall) }
        },
        leadingSlot = leadingIcon,
        trailingSlot = trailingIcon,
        onClick = onClick,
        modifier = modifier,
    )
}
