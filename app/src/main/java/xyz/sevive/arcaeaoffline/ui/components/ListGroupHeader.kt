package xyz.sevive.arcaeaoffline.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import xyz.sevive.arcaeaoffline.ui.theme.header
import xyz.sevive.arcaeaoffline.ui.theme.spacing

/**
 * Group header for items in an edge-to-edge scrolling list.
 * Place it directly in the list, not inside a horizontally padded container.
 *
 * For headers above [BaseSettingsItem] rows, prefer [SettingsGroupHeader].
 */
@Composable
fun ListGroupHeader(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier.padding(
                horizontal = MaterialTheme.spacing.lg,
                vertical = MaterialTheme.spacing.sm,
            ),
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.typography.header.color,
            LocalTextStyle provides MaterialTheme.typography.header,
        ) {
            content()
        }
    }
}

@Composable
fun ListGroupHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    ListGroupHeader(modifier = modifier) {
        Text(text)
    }
}
