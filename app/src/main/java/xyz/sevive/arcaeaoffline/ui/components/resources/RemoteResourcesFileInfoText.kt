package xyz.sevive.arcaeaoffline.ui.components.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.api.ArcaeaResourcesRemoteFileInfo
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedDateTime
import kotlin.time.Instant

@Composable
fun remoteResourcesFileInfoText(fileInfo: ArcaeaResourcesRemoteFileInfo?): String? {
    if (fileInfo == null) return null
    if (!fileInfo.isAvailable) return fileInfo.errorText ?: stringResource(R.string.general_unknown)

    val builtAtText =
        remember(fileInfo.builtAt) {
            fileInfo.builtAt?.let { Instant.fromEpochMilliseconds(it).formatAsLocalizedDateTime() }
        }?.let { stringResource(R.string.general_updated_at, it) }

    return buildString {
        append(fileInfo.version ?: stringResource(R.string.general_unknown))
        append(" (")
        append(builtAtText ?: stringResource(R.string.general_unknown))
        append(")")
    }
}
