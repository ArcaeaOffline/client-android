package xyz.sevive.arcaeaoffline.ui.components.preferences

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha


@Composable
fun CheckboxPreferencesWidget(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    title: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    leadingSlot: (@Composable () -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    BasePreferencesWidget(
        onClick = { onValueChange(!value) },
        title = title,
        modifier = modifier,
        content = content,
        leadingSlot = leadingSlot,
        trailingSlot = {
            Checkbox(
                checked = value,
                onCheckedChange = null,
            )
        },
    )
}

@Composable
fun CheckboxPreferencesWidget(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    description: String? = null,
) {
    CheckboxPreferencesWidget(
        value = value,
        onValueChange = onValueChange,
        title = { Text(title) },
        leadingSlot = icon?.let { { Icon(icon, contentDescription = null, tint = iconTint) } },
        modifier = modifier,
        content = description?.let { { Text(description, Modifier.secondaryItemAlpha()) } },
    )
}
