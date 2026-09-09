package xyz.sevive.arcaeaoffline.ui.components.resources

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.api.ArcaeaResourcesRemoteFileInfo
import xyz.sevive.arcaeaoffline.core.api.DownloadableResource
import xyz.sevive.arcaeaoffline.core.api.RemoteResourcesInfoUiState
import xyz.sevive.arcaeaoffline.ui.components.preferences.TextPreferencesWidget

@Composable
private fun contentFor(
    fileInfo: ArcaeaResourcesRemoteFileInfo?,
    isDownloading: Boolean,
    downloadErrorText: String?,
    refreshErrorText: String?,
): String? =
    when {
        isDownloading -> stringResource(R.string.general_downloading)

        downloadErrorText != null -> downloadErrorText

        fileInfo != null -> remoteResourcesFileInfoText(fileInfo)

        // Per-file info is absent only when the whole refresh failed; fall back to its error text.
        refreshErrorText != null -> refreshErrorText

        else -> null
    }

@Composable
fun RemoteResourceDownloadItem(
    resource: DownloadableResource,
    infoState: RemoteResourcesInfoUiState,
    title: String,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier,
    isDownloading: Boolean = false,
    /** Another ih.db writer (manual import) is running; the two flows share the staging file. */
    isOtherWriteRunning: Boolean = false,
    downloadErrorText: String? = null,
    trailingSlot: (@Composable () -> Unit)? = null,
) {
    val fileInfo = infoState.info?.get(resource)

    TextPreferencesWidget(
        onClick = onDownload,
        // fileInfo is null only while remote info is unknown (refresh failed or not yet fetched);
        // downloading on an unprobed path would just 404, so wait for a successful refresh.
        enabled = !infoState.isFetching && !isDownloading && !isOtherWriteRunning && fileInfo?.isAvailable == true,
        title = title,
        content = contentFor(fileInfo, isDownloading, downloadErrorText, infoState.errorText),
        leadingSlot = {
            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        },
        trailingSlot = trailingSlot,
        modifier = modifier,
    )
}
