package xyz.sevive.arcaeaoffline.ui.screens.ocr.queue

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.ui.components.preferences.TextPreferencesWidget
import xyz.sevive.arcaeaoffline.ui.theme.ArcaeaOfflineTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun OcrQueueAddTaskBottomSheet(
    onDismissRequest: () -> Unit,
    onPickImagesRequest: () -> Unit,
    onPickFolderRequest: () -> Unit,
    onStopJobRequest: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest, modifier = modifier) {
        ExpandedContent(
            onPickImagesRequest = onPickImagesRequest,
            onPickFolderRequest = onPickFolderRequest,
            onStopJobRequest = onStopJobRequest,
        )
    }
}

private object UiVariables {
    val pickImagesIcon = Icons.Default.PhotoLibrary
    val pickFolderIcon
        @Composable get() = ImageVector.vectorResource(R.drawable.ic_folder_image)
    val stopJobIcon = Icons.Default.Stop

    val pickImagesLabel
        @Composable get() = stringResource(R.string.ocr_queue_pick_images_button)
    val pickFolderLabel
        @Composable get() = stringResource(R.string.ocr_queue_pick_folder_button)
    val stopJobLabel
        @Composable get() = stringResource(R.string.ocr_queue_stop_enqueue_checker_job_button)

    val stopJobContentColor
        @Composable get() = MaterialTheme.colorScheme.error
}

@Composable
private fun RowScope.ExpandedContentBigButton(
    onClick: () -> Unit, icon: ImageVector, label: String, modifier: Modifier = Modifier,
) {
    Box(
        Modifier
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp)
            .weight(1f)
            .then(modifier),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.list_padding)),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                icon,
                contentDescription = null,
                Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun ExpandedContent(
    onPickImagesRequest: () -> Unit,
    onPickFolderRequest: () -> Unit,
    onStopJobRequest: (() -> Unit)?,
) {
    Column {
        onStopJobRequest?.let {
            CompositionLocalProvider(
                LocalContentColor provides UiVariables.stopJobContentColor
            ) {
                TextPreferencesWidget(
                    onClick = it,
                    title = UiVariables.stopJobLabel,
                    leadingIcon = { Icon(UiVariables.stopJobIcon, contentDescription = null) },
                )
            }
        }

        Row(Modifier.height(IntrinsicSize.Min)) {
            ExpandedContentBigButton(
                onClick = onPickImagesRequest,
                icon = UiVariables.pickImagesIcon,
                label = UiVariables.pickImagesLabel,
                modifier = Modifier.fillMaxHeight(),
            )

            ExpandedContentBigButton(
                onClick = onPickFolderRequest,
                icon = UiVariables.pickFolderIcon,
                label = UiVariables.pickFolderLabel,
                modifier = Modifier.fillMaxHeight(),
            )
        }
    }
}

@Composable
private fun DefaultContent(
    onPickImagesRequest: () -> Unit,
    onPickFolderRequest: () -> Unit,
    onStopJobRequest: (() -> Unit)?,
) {
    Column {
        onStopJobRequest?.let {
            CompositionLocalProvider(
                LocalContentColor provides UiVariables.stopJobContentColor
            ) {
                TextPreferencesWidget(
                    onClick = it,
                    title = UiVariables.stopJobLabel,
                    leadingIcon = { Icon(UiVariables.stopJobIcon, contentDescription = null) },
                )
            }
        }

        TextPreferencesWidget(
            onClick = onPickImagesRequest,
            title = UiVariables.pickImagesLabel,
            leadingIcon = {
                Icon(
                    UiVariables.pickImagesIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
        )

        TextPreferencesWidget(
            onClick = onPickFolderRequest,
            title = UiVariables.pickFolderLabel,
            leadingIcon = {
                Icon(
                    UiVariables.pickFolderIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            },
        )
    }
}

@PreviewLightDark
@Composable
private fun ExpandedContentPreview() {
    ArcaeaOfflineTheme {
        Surface {
            ExpandedContent(
                onPickImagesRequest = {},
                onPickFolderRequest = {},
                onStopJobRequest = {},
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun DefaultContentPreview() {
    ArcaeaOfflineTheme {
        Surface {
            DefaultContent(
                onPickImagesRequest = {},
                onPickFolderRequest = {},
                onStopJobRequest = {},
            )
        }
    }
}
