package xyz.sevive.arcaeaoffline.ui.components.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


private val HorizontalPadding
    @Composable
    get() = dimensionResource(R.dimen.pref_widget_horizontal_padding)
private val VerticalPadding
    @Composable
    get() = dimensionResource(R.dimen.pref_widget_vertical_padding)

@Composable
fun BasePreferencesWidget(
    title: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    leadingSlot: (@Composable () -> Unit)? = null,
    trailingSlot: (@Composable () -> Unit)? = null,
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
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyLarge
            ) {
                title(this@Column)
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
private fun BasePreferencesWidgetPreview() {
    ArcaeaOfflineTheme {
        Surface {
            BasePreferencesWidget(
                title = { Text("Title") },
                leadingSlot = { Icon(Icons.Default.BugReport, contentDescription = null) },
                trailingSlot = { Text("Trailing") },
            ) {
                Text("Lorem ipsum dolor sit amet", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
