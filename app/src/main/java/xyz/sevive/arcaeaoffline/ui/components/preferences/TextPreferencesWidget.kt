package xyz.sevive.arcaeaoffline.ui.components.preferences

import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import xyz.sevive.arcaeaoffline.helpers.DISABLED_ALPHA
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha


@Composable
fun TextPreferencesWidget(
    title: String,
    modifier: Modifier = Modifier,
    content: String? = null,
    leadingSlot: (@Composable () -> Unit)? = null,
    trailingSlot: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    CompositionLocalProvider(
        LocalContentColor provides LocalContentColor.current.copy(
            alpha = if (enabled) 1f else DISABLED_ALPHA
        )
    ) {
        BasePreferencesWidget(
            title = { Text(title) },
            content = content?.let {
                {
                    Text(
                        it,
                        Modifier.secondaryItemAlpha(),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
            leadingSlot = leadingSlot,
            trailingSlot = trailingSlot,
            onClick = if (enabled) onClick else null,
            modifier = modifier,
        )
    }
}

@Composable
fun TextPreferencesWidget(
    title: String,
    modifier: Modifier = Modifier,
    content: String? = null,
    leadingIcon: ImageVector? = null,
    leadingIconTint: Color = MaterialTheme.colorScheme.primary,
    trailingIcon: ImageVector? = null,
    trailingIconTint: Color = LocalContentColor.current,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
) {
    TextPreferencesWidget(
        title = title,
        content = content,
        leadingSlot = leadingIcon?.let {
            { Icon(it, contentDescription = null, tint = leadingIconTint) }
        },
        trailingSlot = trailingIcon?.let {
            { Icon(it, contentDescription = null, tint = trailingIconTint) }
        },
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    )
}
