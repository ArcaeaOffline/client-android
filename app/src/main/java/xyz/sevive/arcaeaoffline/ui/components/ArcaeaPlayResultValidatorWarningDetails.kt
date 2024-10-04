package xyz.sevive.arcaeaoffline.ui.components

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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.sp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorMaxRecallOverflowWarning
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorPflOverflowWarning
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorPureMemoryFarLostNotZeroWarning
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPlayResultValidatorWarning
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme
import xyz.sevive.arcaeaoffline.ui.theme.extendedColorScheme

@Composable
private fun ArcaeaPlayResultValidatorWarningDetailItem(
    warning: ArcaeaPlayResultValidatorWarning,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val expandArrowRotateDegree by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f, label = "expandArrow"
    )

    val context = LocalContext.current
    val density = LocalDensity.current

    val padding = dimensionResource(R.dimen.card_padding)
    val messagePadding = remember(density) {
        val start = with(density) { 24.sp.toDp() + padding * 2 }
        PaddingValues(start = start, end = padding, bottom = padding)
    }

    val title = remember(warning) { warning.getTitle(context) }
    val message = remember(warning) { warning.getMessage(context) }

    val showWarningId by remember {
        derivedStateOf { message.isNullOrBlank() || expanded }
    }

    Column(
        Modifier
            .clickable { expanded = !expanded }
            .then(modifier)) {
        Row(
            Modifier
                .minimumInteractiveComponentSize()
                .padding(padding),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.extendedColorScheme.warning
            ) {
                Icon(Icons.Default.ErrorOutline, contentDescription = null)

                Column(Modifier.weight(1f)) {
                    Text(title)
                    AnimatedVisibility(visible = showWarningId) {
                        Text(
                            warning.id,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Light)
                        )
                    }
                }
            }

            if (!message.isNullOrBlank()) {
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    Modifier.graphicsLayer { rotationZ = expandArrowRotateDegree },
                )
            }
        }

        AnimatedVisibility(visible = expanded) {
            if (!message.isNullOrBlank()) {
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
fun ArcaeaPlayResultValidatorWarningDetails(
    warnings: List<ArcaeaPlayResultValidatorWarning>,
    modifier: Modifier = Modifier,
) {
    Card(modifier) {
        warnings.forEachIndexed { i, it ->
            ArcaeaPlayResultValidatorWarningDetailItem(warning = it)
            if (i != warnings.lastIndex) HorizontalDivider()
        }
    }
}

@Composable
fun ArcaeaPlayResultValidatorWarningDetailsDialog(
    onDismissRequest: () -> Unit,
    warnings: List<ArcaeaPlayResultValidatorWarning>,
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
                item { ArcaeaPlayResultValidatorWarningDetails(warnings) }
            }
        },
        confirmButton = { /* Nothing */ },
    )
}

@PreviewLightDark
@Composable
private fun ArcaeaPlayResultValidatorWarningDetailItemPreview() {
    ArcaeaOfflineTheme {
        Surface {
            ArcaeaPlayResultValidatorWarningDetailItem(ArcaeaPlayResultValidatorPflOverflowWarning)
        }
    }
}

@PreviewLightDark
@Composable
private fun ArcaeaPlayResultValidatorWarningDetailsPreview() {
    ArcaeaOfflineTheme {
        Surface {
            ArcaeaPlayResultValidatorWarningDetails(
                warnings = listOf(
                    ArcaeaPlayResultValidatorPflOverflowWarning,
                    ArcaeaPlayResultValidatorMaxRecallOverflowWarning,
                    ArcaeaPlayResultValidatorPureMemoryFarLostNotZeroWarning,
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ArcaeaPlayResultValidatorWarningDetailsDialogPreview() {
    ArcaeaOfflineTheme {
        Surface {
            ArcaeaPlayResultValidatorWarningDetailsDialog(
                onDismissRequest = {},
                warnings = listOf(
                    ArcaeaPlayResultValidatorPflOverflowWarning,
                    ArcaeaPlayResultValidatorMaxRecallOverflowWarning,
                    ArcaeaPlayResultValidatorPureMemoryFarLostNotZeroWarning,
                ),
            )
        }
    }
}
