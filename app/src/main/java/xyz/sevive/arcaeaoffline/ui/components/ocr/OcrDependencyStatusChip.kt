package xyz.sevive.arcaeaoffline.ui.components.ocr

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.twotone.DashboardCustomize
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.ocr.OcrDependencyStatus
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


data class OcrDependencyStatusChipUiState(
    val icon: ImageVector = Icons.Default.QuestionMark,
    val text: String = "Unknown",
    val color: Color = Color.Unspecified,
    val expanded: Boolean = false,
) {
    companion object {
        val OK: OcrDependencyStatusChipUiState
            @Composable get() = OcrDependencyStatusChipUiState(
                icon = Icons.Default.Check,
                text = stringResource(R.string.general_ok),
                color = MaterialTheme.colorScheme.primary,
            )

        val WARNING: OcrDependencyStatusChipUiState
            @Composable get() = OcrDependencyStatusChipUiState(
                icon = Icons.Default.Warning,
                text = "WARNING",
                color = MaterialTheme.colorScheme.tertiary,
            )

        val ERROR: OcrDependencyStatusChipUiState
            @Composable get() = OcrDependencyStatusChipUiState(
                icon = Icons.Default.ErrorOutline,
                text = "ERROR",
                color = MaterialTheme.colorScheme.error,
            )

        val UNKNOWN: OcrDependencyStatusChipUiState
            @Composable get() = OcrDependencyStatusChipUiState(
                icon = Icons.Default.QuestionMark,
                text = "UNKNOWN",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

        val ABSENCE: OcrDependencyStatusChipUiState
            @Composable get() = OcrDependencyStatusChipUiState(
                icon = ImageVector.vectorResource(R.drawable.ic_file_hidden),
                text = "404",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
    }
}

@Composable
fun OcrDependencyStatusChip(
    uiState: OcrDependencyStatusChipUiState, modifier: Modifier = Modifier
) {
    val padding by animateDpAsState(
        targetValue = if (uiState.expanded) dimensionResource(R.dimen.icon_text_padding) else 0.dp,
        label = "iconPadding"
    )

    CompositionLocalProvider(
        LocalContentColor provides uiState.color,
        LocalTextStyle provides MaterialTheme.typography.labelLarge
    ) {
        Row(Modifier.then(modifier), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                uiState.icon,
                contentDescription = null,
                Modifier
                    .padding(end = padding)
                    .size(AssistChipDefaults.IconSize)
            )
            AnimatedVisibility(visible = uiState.expanded) {
                Text(uiState.text)
            }
        }
    }
}


@Composable
fun OcrDependencyStatusChip(
    status: OcrDependencyStatus,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
) {
    when (status) {
        OcrDependencyStatus.OK -> OcrDependencyStatusChipUiState.OK
        OcrDependencyStatus.WARNING -> OcrDependencyStatusChipUiState.WARNING
        OcrDependencyStatus.ERROR -> OcrDependencyStatusChipUiState.ERROR
        OcrDependencyStatus.ABSENCE -> OcrDependencyStatusChipUiState.ABSENCE
        else -> OcrDependencyStatusChipUiState.UNKNOWN
    }.let {
        OcrDependencyStatusChip(uiState = it.copy(expanded = expanded), modifier = modifier)
    }
}

@Preview
@Composable
private fun OcrDependencyStatusChipSinglePreview() {
    var expanded by remember { mutableStateOf(false) }

    ArcaeaOfflineTheme {
        Surface(
            Modifier
                .width(200.dp)
                .padding(8.dp)
        ) {
            OcrDependencyStatusChip(
                OcrDependencyStatus.UNKNOWN,
                modifier = Modifier.clickable { expanded = !expanded },
                expanded = expanded
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun OcrDependencyStatusChipPreview() {
    var expanded by remember { mutableStateOf(false) }

    ArcaeaOfflineTheme {
        Surface {
            Column {
                Button(onClick = { expanded = !expanded }) { Text("e = !e") }

                Text("Presets")
                Column {
                    OcrDependencyStatusChip(OcrDependencyStatus.OK, expanded = expanded)
                    OcrDependencyStatusChip(OcrDependencyStatus.WARNING, expanded = expanded)
                    OcrDependencyStatusChip(OcrDependencyStatus.ERROR, expanded = expanded)
                    OcrDependencyStatusChip(OcrDependencyStatus.ABSENCE, expanded = expanded)
                    OcrDependencyStatusChip(OcrDependencyStatus.UNKNOWN, expanded = expanded)
                }

                HorizontalDivider()

                Text("Custom")
                Column {
                    OcrDependencyStatusChip(
                        OcrDependencyStatusChipUiState(
                            icon = Icons.TwoTone.DashboardCustomize,
                            text = "Lor ips",
                            color = Color.Red,
                            expanded = true,
                        )
                    )
                }
            }
        }
    }
}
