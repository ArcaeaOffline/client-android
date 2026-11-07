package xyz.sevive.arcaeaoffline.ui.components.playresult

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.stringResource
import xyz.sevive.arcaeaoffline.core.validation.PlayResultFinding
import xyz.sevive.arcaeaoffline.core.validation.PlayResultWarning
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.extendedColorScheme
import xyz.sevive.arcaeaoffline.ui.theme.spacing
import xyz.sevive.arcaeaoffline.ui.utils.secondaryItemAlpha

@Composable
private fun PlayResultWarningDetailItem(
    finding: PlayResultFinding,
    modifier: Modifier = Modifier,
) {
    val warning = finding.warning
    var expanded by rememberSaveable { mutableStateOf(false) }
    val expandArrowRotateDegree by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "expandArrow",
    )

    val density = LocalDensity.current

    val padding = MaterialTheme.spacing.cardPadding
    val messagePadding =
        remember(density) {
            val start = with(density) { 24.sp.toDp() + padding * 2 }
            PaddingValues(start = start, end = padding, bottom = padding)
        }

    val title = stringResource(warning.titleRes())
    val message = stringResource(warning.messageRes())

    Column(
        Modifier
            .clickable { expanded = !expanded }
            .then(modifier),
    ) {
        Row(
            Modifier
                .minimumInteractiveComponentSize()
                .padding(padding),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.extendedColorScheme.warning,
            ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null)

                Column(Modifier.weight(1f)) {
                    Text(title)
                    AnimatedVisibility(visible = expanded) {
                        Text(
                            warning.name,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Light),
                        )
                    }
                }
            }

            if (message.isNotBlank()) {
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    Modifier.graphicsLayer { rotationZ = expandArrowRotateDegree },
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            if (message.isNotBlank()) {
                Text(
                    message,
                    Modifier
                        .secondaryItemAlpha()
                        .padding(messagePadding),
                )
            }
        }
    }
}

@Composable
fun PlayResultWarningDetails(
    warnings: List<PlayResultFinding>,
    modifier: Modifier = Modifier,
) {
    Card(modifier) {
        warnings.forEachIndexed { i, it ->
            PlayResultWarningDetailItem(finding = it)
            if (i != warnings.lastIndex) HorizontalDivider()
        }
    }
}

@Composable
fun PlayResultWarningDetailsDialog(
    onDismissRequest: () -> Unit,
    warnings: List<PlayResultFinding>,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        icon = {
            BadgedBox(
                badge = {
                    Badge(
                        contentColor = MaterialTheme.extendedColorScheme.warning,
                        containerColor = MaterialTheme.extendedColorScheme.warningContainer,
                    ) {
                        Text(warnings.size.toString())
                    }
                },
            ) {
                Icon(Icons.Default.Warning, contentDescription = null)
            }
        },
        text = {
            LazyColumn {
                item { PlayResultWarningDetails(warnings) }
            }
        },
        confirmButton = { /* Nothing */ },
    )
}

@Preview
@Composable
private fun PlayResultWarningDetailItemPreview() {
    ArcaeaOfflineTheme {
        Surface {
            PlayResultWarningDetailItem(PlayResultFinding(PlayResultWarning.PFL_OVERFLOW))
        }
    }
}

@Preview
@Composable
private fun PlayResultWarningDetailsPreview() {
    ArcaeaOfflineTheme {
        Surface {
            PlayResultWarningDetails(
                warnings =
                    listOf(
                        PlayResultFinding(PlayResultWarning.PFL_OVERFLOW),
                        PlayResultFinding(PlayResultWarning.MAX_RECALL_OVERFLOW),
                        PlayResultFinding(PlayResultWarning.PURE_MEMORY_FAR_LOST_NOT_ZERO),
                    ),
            )
        }
    }
}

@Preview
@Composable
private fun PlayResultWarningDetailsDialogPreview() {
    ArcaeaOfflineTheme {
        Surface {
            PlayResultWarningDetailsDialog(
                onDismissRequest = {},
                warnings =
                    listOf(
                        PlayResultFinding(PlayResultWarning.PFL_OVERFLOW),
                        PlayResultFinding(PlayResultWarning.MAX_RECALL_OVERFLOW),
                        PlayResultFinding(PlayResultWarning.PURE_MEMORY_FAR_LOST_NOT_ZERO),
                    ),
            )
        }
    }
}
