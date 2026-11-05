package xyz.sevive.arcaeaoffline.ui.screens.resources

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowSizeClass
import org.koin.androidx.compose.koinViewModel
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.api.DownloadableResource
import xyz.sevive.arcaeaoffline.core.api.RemoteResourcesInfoUiState
import xyz.sevive.arcaeaoffline.helpers.formatAsLocalizedDateTime
import xyz.sevive.arcaeaoffline.helpers.rememberFileChooserLauncher
import xyz.sevive.arcaeaoffline.helpers.secondaryItemAlpha
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaAppIcon
import xyz.sevive.arcaeaoffline.ui.components.ImportLogBottomSheet
import xyz.sevive.arcaeaoffline.ui.components.SelectItem
import xyz.sevive.arcaeaoffline.ui.components.SelectOption
import xyz.sevive.arcaeaoffline.ui.components.TextItem
import xyz.sevive.arcaeaoffline.ui.components.resources.RemoteResourceDownloadItem
import xyz.sevive.arcaeaoffline.ui.theme.spacing
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(
    modifier: Modifier = Modifier,
    viewModel: ResourcesViewModel = koinViewModel(),
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val songsCardStatus by viewModel.songsCardStatus.collectAsStateWithLifecycle()
    val chartInfoCardStatus by viewModel.chartInfoCardStatus.collectAsStateWithLifecycle()
    val canImportLists by viewModel.canImportLists.collectAsStateWithLifecycle()

    var showLogsSheet by rememberSaveable { mutableStateOf(false) }
    if (showLogsSheet) {
        ImportLogBottomSheet(
            onDismissRequest = { showLogsSheet = false },
            logs = uiState.logs,
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.nav_resources)) },
                actions = {
                    Box(contentAlignment = Alignment.Center) {
                        IconButton(
                            onClick = viewModel::refreshRemoteResourcesInfo,
                            enabled = !uiState.remoteResourcesInfoState.isFetching,
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                        }

                        if (uiState.remoteResourcesInfoState.isFetching) {
                            CircularProgressIndicator()
                        }
                    }

                    Box(contentAlignment = Alignment.Center) {
                        IconButton(onClick = { showLogsSheet = true }) {
                            Icon(Icons.Default.PendingActions, contentDescription = null)
                        }

                        if (uiState.isWorking) {
                            CircularProgressIndicator()
                        }
                    }
                },
            )
        },
    ) { padding ->
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        val columnCount = if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) 2 else 1

        FlowRow(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        bottom = MaterialTheme.spacing.pagePadding,
                        start = MaterialTheme.spacing.pagePadding,
                        end = MaterialTheme.spacing.pagePadding,
                    ),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
            maxItemsInEachRow = columnCount,
        ) {
            ResourcesSongsCard(
                status = songsCardStatus,
                remoteResourcesInfoState = uiState.remoteResourcesInfoState,
                downloadingResources = uiState.downloadingResources,
                downloadErrorTexts = uiState.downloadErrorTexts,
                canImportLists = canImportLists,
                onDownloadPacklist = viewModel::downloadPacklist,
                onDownloadSonglist = { viewModel.downloadSonglist(context) },
                onImportPacklist = { viewModel.importPacklist(it, context) },
                onImportSonglist = { viewModel.importSonglist(it, context) },
                onImportArcaeaApk = { viewModel.importArcaeaApkFromSelected(it, context) },
                onImportFromInstalledArcaea = { viewModel.importArcaeaApkFromInstalled(context) },
                modifier = Modifier.weight(1f),
            )

            ResourcesChartInfoCard(
                status = chartInfoCardStatus,
                remoteResourcesInfoState = uiState.remoteResourcesInfoState,
                downloadingResources = uiState.downloadingResources,
                downloadErrorTexts = uiState.downloadErrorTexts,
                onDownload = { viewModel.downloadChartInfoDatabase(context) },
                onImport = { viewModel.importChartsInfoDatabase(it, context) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

private enum class SongsSource {
    ONLINE,
    FILE,
    INSTALLED,
}

private enum class ChartInfoSource {
    ONLINE,
    FILE,
}

@Composable
private fun LastImportedText(
    label: String,
    instant: Instant,
    modifier: Modifier = Modifier,
) {
    Text(
        "$label · ${stringResource(R.string.resources_last_imported_at, instant.formatAsLocalizedDateTime())}",
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier.secondaryItemAlpha(),
    )
}

@Composable
private fun ResourcesSongsCard(
    status: ResourcesViewModel.SongsCardStatus,
    remoteResourcesInfoState: RemoteResourcesInfoUiState,
    downloadingResources: Set<DownloadableResource>,
    downloadErrorTexts: Map<DownloadableResource, String>,
    canImportLists: Boolean,
    onDownloadPacklist: () -> Unit,
    onDownloadSonglist: () -> Unit,
    onImportPacklist: (Uri) -> Unit,
    onImportSonglist: (Uri) -> Unit,
    onImportArcaeaApk: (Uri) -> Unit,
    onImportFromInstalledArcaea: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var source by rememberSaveable { mutableStateOf(SongsSource.ONLINE) }
    val effectiveSource = if (source == SongsSource.INSTALLED && !canImportLists) SongsSource.ONLINE else source
    val sourceOptions =
        buildList {
            add(SelectOption(value = SongsSource.ONLINE, label = stringResource(R.string.resources_source_online)))
            add(SelectOption(value = SongsSource.FILE, label = stringResource(R.string.resources_source_file)))
            if (canImportLists) {
                add(
                    SelectOption(
                        value = SongsSource.INSTALLED,
                        label = stringResource(R.string.database_manage_import_from_arcaea_installed),
                    ),
                )
            }
        }

    val statusString =
        listOf(
            pluralStringResource(R.plurals.database_pack_entries, status.packCount, status.packCount),
            pluralStringResource(R.plurals.database_song_entries, status.songCount, status.songCount),
            pluralStringResource(R.plurals.database_difficulty_entries, status.difficultyCount, status.difficultyCount),
        ).joinToString(" · ")

    val importPacklistLauncher = rememberFileChooserLauncher { it?.let(onImportPacklist) }
    val importSonglistLauncher = rememberFileChooserLauncher { it?.let(onImportSonglist) }
    val importArcaeaApkLauncher = rememberFileChooserLauncher { it?.let(onImportArcaeaApk) }

    Card(modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(
                horizontal = MaterialTheme.spacing.pagePadding,
                vertical = MaterialTheme.spacing.cardPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        ) {
            Text(
                stringResource(R.string.resources_songs_title),
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                statusString,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.secondaryItemAlpha(),
            )

            status.packlistLastImportedAt?.let { LastImportedText(stringResource(R.string.database_manage_download_packlist), it) }
            status.songsLastImportedAt?.let { LastImportedText(stringResource(R.string.database_manage_download_songlist), it) }
        }

        HorizontalDivider()

        SelectItem(
            options = sourceOptions,
            selected = effectiveSource,
            onSelect = { source = it },
        )

        HorizontalDivider()

        when (effectiveSource) {
            SongsSource.ONLINE -> {
                RemoteResourceDownloadItem(
                    resource = DownloadableResource.PACKLIST,
                    infoState = remoteResourcesInfoState,
                    isDownloading = DownloadableResource.PACKLIST in downloadingResources,
                    downloadErrorText = downloadErrorTexts[DownloadableResource.PACKLIST],
                    title = stringResource(R.string.database_manage_download_packlist),
                    onDownload = onDownloadPacklist,
                )

                RemoteResourceDownloadItem(
                    resource = DownloadableResource.SONGLIST,
                    infoState = remoteResourcesInfoState,
                    isDownloading = DownloadableResource.SONGLIST in downloadingResources,
                    downloadErrorText = downloadErrorTexts[DownloadableResource.SONGLIST],
                    title = stringResource(R.string.database_manage_download_songlist),
                    onDownload = onDownloadSonglist,
                )
            }

            SongsSource.FILE -> {
                TextItem(
                    onClick = { importPacklistLauncher.launch("*/*") },
                    title = stringResource(R.string.database_manage_import_packlist),
                    leadingIcon = Icons.Default.DataObject,
                )

                TextItem(
                    onClick = { importSonglistLauncher.launch("*/*") },
                    title = stringResource(R.string.database_manage_import_songlist),
                    leadingIcon = Icons.Default.DataObject,
                )

                TextItem(
                    onClick = { importArcaeaApkLauncher.launch("*/*") },
                    title = stringResource(R.string.database_manage_import_from_arcaea_apk),
                    leadingIcon = Icons.Default.Android,
                )
            }

            SongsSource.INSTALLED -> {
                TextItem(
                    onClick = onImportFromInstalledArcaea,
                    title = stringResource(R.string.database_manage_import_from_arcaea_installed),
                    leadingSlot = { ArcaeaAppIcon() },
                )
            }
        }
    }
}

@Composable
private fun ResourcesChartInfoCard(
    status: ResourcesViewModel.ChartInfoCardStatus,
    remoteResourcesInfoState: RemoteResourcesInfoUiState,
    downloadingResources: Set<DownloadableResource>,
    downloadErrorTexts: Map<DownloadableResource, String>,
    onDownload: () -> Unit,
    onImport: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    var source by rememberSaveable { mutableStateOf(ChartInfoSource.ONLINE) }

    val importLauncher = rememberFileChooserLauncher { it?.let(onImport) }
    val sourceOptions =
        listOf(
            SelectOption(value = ChartInfoSource.ONLINE, label = stringResource(R.string.resources_source_online)),
            SelectOption(value = ChartInfoSource.FILE, label = stringResource(R.string.resources_source_file)),
        )

    Card(modifier.fillMaxWidth()) {
        Column(
            Modifier.padding(
                horizontal = MaterialTheme.spacing.pagePadding,
                vertical = MaterialTheme.spacing.cardPadding,
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs),
        ) {
            Text(
                stringResource(R.string.resources_chart_info_title),
                style = MaterialTheme.typography.titleMedium,
            )

            Text(
                pluralStringResource(R.plurals.database_chart_info_entries, status.chartInfoCount, status.chartInfoCount),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.secondaryItemAlpha(),
            )

            if (status.missingChartInfoCount > 0) {
                Text(
                    pluralStringResource(
                        R.plurals.resources_missing_chart_info,
                        status.missingChartInfoCount,
                        status.missingChartInfoCount,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.secondaryItemAlpha(),
                )
            }
            status.chartInfoLastImportedAt?.let {
                LastImportedText(
                    stringResource(R.string.database_manage_download_chart_info_database),
                    it,
                )
            }
        }

        HorizontalDivider()

        SelectItem(
            options = sourceOptions,
            selected = source,
            onSelect = { source = it },
        )

        HorizontalDivider()

        when (source) {
            ChartInfoSource.ONLINE -> {
                RemoteResourceDownloadItem(
                    resource = DownloadableResource.CHART_INFO_DATABASE,
                    infoState = remoteResourcesInfoState,
                    isDownloading = DownloadableResource.CHART_INFO_DATABASE in downloadingResources,
                    downloadErrorText = downloadErrorTexts[DownloadableResource.CHART_INFO_DATABASE],
                    title = stringResource(R.string.database_manage_download_chart_info_database),
                    onDownload = onDownload,
                )
            }

            ChartInfoSource.FILE -> {
                TextItem(
                    onClick = { importLauncher.launch("*/*") },
                    title = stringResource(R.string.database_manage_import_chart_info_database),
                    leadingIcon = Icons.Default.FileOpen,
                )
            }
        }
    }
}
