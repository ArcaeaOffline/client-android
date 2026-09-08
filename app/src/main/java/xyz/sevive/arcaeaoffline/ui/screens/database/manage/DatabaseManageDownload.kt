package xyz.sevive.arcaeaoffline.ui.screens.database.manage

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.api.DownloadableResource
import xyz.sevive.arcaeaoffline.core.api.RemoteResourcesInfoUiState
import xyz.sevive.arcaeaoffline.ui.components.resources.RemoteResourceDownloadItem

@Composable
fun DatabaseManageDownload(
    remoteResourcesInfoState: RemoteResourcesInfoUiState,
    downloadingResources: Set<DownloadableResource>,
    onDownloadPacklist: () -> Unit,
    onDownloadSonglist: () -> Unit,
    onDownloadChartInfoDatabase: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        RemoteResourceDownloadItem(
            resource = DownloadableResource.PACKLIST,
            infoState = remoteResourcesInfoState,
            isDownloading = DownloadableResource.PACKLIST in downloadingResources,
            title = stringResource(R.string.database_manage_download_packlist),
            onDownload = onDownloadPacklist,
        )

        RemoteResourceDownloadItem(
            resource = DownloadableResource.SONGLIST,
            infoState = remoteResourcesInfoState,
            isDownloading = DownloadableResource.SONGLIST in downloadingResources,
            title = stringResource(R.string.database_manage_download_songlist),
            onDownload = onDownloadSonglist,
        )

        RemoteResourceDownloadItem(
            resource = DownloadableResource.CHART_INFO_DATABASE,
            infoState = remoteResourcesInfoState,
            isDownloading = DownloadableResource.CHART_INFO_DATABASE in downloadingResources,
            title = stringResource(R.string.database_manage_download_chart_info_database),
            onDownload = onDownloadChartInfoDatabase,
        )
    }
}
