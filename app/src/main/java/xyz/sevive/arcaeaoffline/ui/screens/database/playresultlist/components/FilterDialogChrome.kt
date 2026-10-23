package xyz.sevive.arcaeaoffline.ui.screens.database.playresultlist.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.theme.spacing

/**
 * Frame shared by every dialog screen: a fixed header row, the screen content
 * and a fixed footer.
 *
 * The caller supplies what the header and footer contain, so this layout knows
 * nothing about individual screens.
 */
@Composable
internal fun FilterDialogChrome(
    fill: Boolean,
    leading: @Composable () -> Unit,
    title: String,
    footer: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        Modifier
            .fillMaxWidth()
            .then(if (fill) Modifier.fillMaxSize() else Modifier),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.spacing.xs, vertical = MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            leading()

            Text(
                text = title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (fill) {
            Box(Modifier.weight(1f).fillMaxWidth()) { content() }
        } else {
            content()
        }

        footer()
    }
}

/**
 * Footer row with an optional left label, one secondary action and the
 * confirming action. Without a label the actions align to the end.
 */
@Composable
internal fun FilterDialogFooter(
    label: String?,
    actionLabel: String,
    onAction: () -> Unit,
    onDone: () -> Unit,
) {
    Surface(tonalElevation = 3.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.pagePadding, vertical = MaterialTheme.spacing.sm),
            horizontalArrangement =
                Arrangement.spacedBy(
                    MaterialTheme.spacing.sm,
                    if (label == null) Alignment.End else Alignment.Start,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (label != null) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            TextButton(onClick = onAction) {
                Text(actionLabel)
            }

            Button(onClick = onDone) {
                Text(stringResource(R.string.general_action_done))
            }
        }
    }
}
