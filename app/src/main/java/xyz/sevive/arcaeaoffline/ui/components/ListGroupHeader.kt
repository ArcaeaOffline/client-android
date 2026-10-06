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
import androidx.compose.ui.text.font.FontWeight
import xyz.sevive.arcaeaoffline.ui.theme.spacing

/**
 * Section header carrying its own horizontal gutter. Place it directly in an
 * edge-to-edge container instead of a horizontally padded container.
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
            LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
            LocalTextStyle provides MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
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
