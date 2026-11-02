package xyz.sevive.arcaeaoffline.ui.components

import androidx.annotation.IntRange
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import xyz.sevive.arcaeaoffline.helpers.DISABLED_ALPHA
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.header
import xyz.sevive.arcaeaoffline.ui.theme.spacing

private val HorizontalPadding
    @Composable
    get() = MaterialTheme.spacing.lg
private val VerticalPadding
    @Composable
    get() = MaterialTheme.spacing.lg

/**
 * Group header whose text aligns with [BaseSettingsItem] titles by sharing
 * the same gutter.
 */
@Composable
fun SettingsGroupHeader(
    text: String,
    modifier: Modifier = Modifier,
) {
    SettingsGroupHeader(modifier = modifier) {
        Text(text)
    }
}

@Composable
fun SettingsGroupHeader(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(
                    top = MaterialTheme.spacing.xl,
                    bottom = MaterialTheme.spacing.sm,
                    start = HorizontalPadding,
                    end = HorizontalPadding,
                ),
        contentAlignment = Alignment.CenterStart,
    ) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.typography.header.color,
            LocalTextStyle provides MaterialTheme.typography.header,
        ) {
            content()
        }
    }
}

/**
 * A settings row whose middle column stacks a title, a description, and custom content.
 *
 * @param onClick Invoked when the row is tapped. `null` makes the row non-clickable.
 * @param description Text shown under the title in the secondary style.
 * @param descriptionMaxLines Maximum lines of [description]. Overflow ends with an ellipsis.
 * @param content Content shown under [description], e.g. a slider or a progress bar.
 */
@Composable
fun BaseSettingsItem(
    title: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leadingSlot: (@Composable () -> Unit)? = null,
    trailingSlot: (@Composable () -> Unit)? = null,
    description: String? = null,
    descriptionMaxLines: Int = Int.MAX_VALUE,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    Row(
        Modifier
            .clickable(onClick != null) { onClick?.invoke() }
            .minimumInteractiveComponentSize()
            .then(modifier),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leadingSlot?.let {
            Box(Modifier.padding(start = HorizontalPadding, end = HorizontalPadding / 2)) { it() }
        }

        Column(
            Modifier
                .weight(1f)
                .padding(horizontal = HorizontalPadding, vertical = VerticalPadding),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xxs),
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyLarge,
            ) {
                title(this@Column)
            }
            description?.let {
                Text(
                    it,
                    Modifier.secondaryItemAlpha(),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = descriptionMaxLines,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            content?.let { it(this@Column) }
        }

        trailingSlot?.let {
            Box(Modifier.padding(start = HorizontalPadding / 2, end = HorizontalPadding)) { it() }
        }
    }
}

@PreviewLightDark
@Composable
private fun BaseSettingsItemPreview() {
    ArcaeaOfflineTheme {
        Surface {
            BaseSettingsItem(
                title = { Text("Title") },
                leadingSlot = { Icon(Icons.Default.BugReport, contentDescription = null) },
                trailingSlot = { Text("Trailing") },
                description = "Lorem ipsum dolor sit amet",
            )
        }
    }
}

@Composable
fun CheckboxItem(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    title: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    leadingSlot: (@Composable () -> Unit)? = null,
    description: String? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    BaseSettingsItem(
        onClick = { onValueChange(!value) },
        title = title,
        modifier = modifier,
        description = description,
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
fun CheckboxItem(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    description: String? = null,
) {
    CheckboxItem(
        value = value,
        onValueChange = onValueChange,
        title = { Text(title) },
        leadingSlot = icon?.let { { Icon(icon, contentDescription = null, tint = iconTint) } },
        modifier = modifier,
        description = description,
    )
}

@Composable
fun SwitchItem(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    title: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    leadingSlot: (@Composable () -> Unit)? = null,
    description: String? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    BaseSettingsItem(
        onClick = { onValueChange(!value) },
        title = title,
        modifier = modifier,
        description = description,
        content = content,
        leadingSlot = leadingSlot,
        trailingSlot = {
            Switch(
                checked = value,
                onCheckedChange = null,
            )
        },
    )
}

@Composable
fun SwitchItem(
    value: Boolean,
    onValueChange: (Boolean) -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconTint: Color = MaterialTheme.colorScheme.primary,
    description: String? = null,
) {
    SwitchItem(
        value = value,
        onValueChange = onValueChange,
        title = { Text(title) },
        leadingSlot = icon?.let { { Icon(icon, contentDescription = null, tint = iconTint) } },
        modifier = modifier,
        description = description,
    )
}

data class SelectOption<T>(
    val value: T,
    val label: String,
    val description: String? = null,
)

@Composable
fun RadioItem(
    selected: Boolean,
    onSelect: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
) {
    BaseSettingsItem(
        onClick = onSelect,
        title = { Text(title) },
        description = description,
        leadingSlot = {
            RadioButton(
                selected = selected,
                onClick = null,
            )
        },
        modifier = modifier,
    )
}

/**
 * A radio-button list of mutually exclusive options that renders one [BaseSettingsItem]
 * row per option without an outer container.
 */
@Composable
fun <T> SelectItem(
    options: List<SelectOption<T>>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        options.forEach { option ->
            RadioItem(
                selected = option.value == selected,
                onSelect = { onSelect(option.value) },
                title = option.label,
                description = option.description,
            )
        }
    }
}

@Composable
fun SliderItem(
    value: Float,
    onValueChange: (Float) -> Unit,
    title: @Composable ColumnScope.() -> Unit,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    @IntRange(from = 0) steps: Int = 0,
    leadingSlot: (@Composable () -> Unit)? = null,
    trailingSlot: (@Composable () -> Unit)? = null,
    description: String? = null,
    content: (@Composable ColumnScope.() -> Unit)? = null,
) {
    BaseSettingsItem(
        title = title,
        description = description,
        leadingSlot = leadingSlot,
        trailingSlot = trailingSlot,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
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
fun SliderItem(
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
    SliderItem(
        value = value,
        onValueChange = onValueChange,
        title = { Text(title) },
        valueRange = valueRange,
        steps = steps,
        leadingSlot = icon?.let { { Icon(icon, contentDescription = null, tint = iconTint) } },
        trailingSlot = trailingSlot,
        description = description,
    )
}

@Composable
fun TextItem(
    title: String,
    modifier: Modifier = Modifier,
    content: String? = null,
    leadingSlot: (@Composable () -> Unit)? = null,
    trailingSlot: (@Composable () -> Unit)? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    contentMaxLines: Int = Int.MAX_VALUE,
) {
    CompositionLocalProvider(
        LocalContentColor provides
            LocalContentColor.current.copy(
                alpha = if (enabled) 1f else DISABLED_ALPHA,
            ),
    ) {
        BaseSettingsItem(
            title = { Text(title) },
            description = content,
            descriptionMaxLines = contentMaxLines,
            leadingSlot = leadingSlot,
            trailingSlot = trailingSlot,
            onClick = if (enabled) onClick else null,
            modifier = modifier,
        )
    }
}

@Composable
fun TextItem(
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
    TextItem(
        title = title,
        content = content,
        leadingSlot =
            leadingIcon?.let {
                { Icon(it, contentDescription = null, tint = leadingIconTint) }
            },
        trailingSlot =
            trailingIcon?.let {
                { Icon(it, contentDescription = null, tint = trailingIconTint) }
            },
        onClick = onClick,
        enabled = enabled,
        modifier = modifier,
    )
}
