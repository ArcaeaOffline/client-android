package xyz.sevive.arcaeaoffline.ui.components.preferences

import androidx.annotation.IntRange
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha


@Composable
fun SliderPreferencesWidget(
    value: Float,
    onValueChange: (Float) -> Unit,
    title: @Composable ColumnScope.() -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0) steps: Int = 0,
    leadingSlot: (@Composable () -> Unit)? = null,
    trailingSlot: (@Composable () -> Unit)? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    BasePreferencesWidget(
        title = title,
        leadingSlot = leadingSlot,
        trailingSlot = trailingSlot,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding))
        ) {
            content?.invoke(this@Column)
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                steps = steps,
            )
        }
    }
}

@Composable
fun SliderPreferencesWidget(
    value: Float,
    onValueChange: (Float) -> Unit,
    title: String,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0) steps: Int = 0,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    trailingSlot: (@Composable () -> Unit)? = null,
    description: String? = null,
) {
    SliderPreferencesWidget(
        value = value,
        onValueChange = onValueChange,
        title = { Text(title) },
        valueRange = valueRange,
        steps = steps,
        leadingSlot = icon?.let { { Icon(icon, contentDescription = null, tint = iconTint) } },
        trailingSlot = trailingSlot,
        content = description?.let { { Text(description, Modifier.secondaryItemAlpha()) } },
    )
}
