package xyz.sevive.arcaeaoffline.ui.screens.ocr.dependencies

import android.content.Context
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.text.format.Formatter
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.utils.div
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import xyz.sevive.arcaeaoffline.core.Progress
import xyz.sevive.arcaeaoffline.core.api.ArcaeaResourcesApiClient
import xyz.sevive.arcaeaoffline.core.api.RemoteResourcesInfoStateHolder
import xyz.sevive.arcaeaoffline.core.api.throwableToErrorText
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabase
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.helpers.ArcaeaResourcesStateHolder
import xyz.sevive.arcaeaoffline.helpers.ImageHashesDatabaseStatusDetail
import xyz.sevive.arcaeaoffline.helpers.OcrDependencyLoader
import xyz.sevive.arcaeaoffline.helpers.OcrDependencyStatusBuilder
import xyz.sevive.arcaeaoffline.helpers.context.copyToCache
import xyz.sevive.arcaeaoffline.helpers.context.uriSizeIfTooLarge
import xyz.sevive.arcaeaoffline.helpers.fromWorkInfo
import xyz.sevive.arcaeaoffline.jobs.ImageHashesDatabaseBuilderJob
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyCrnnModelStatusUiState
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyImageHashesDatabaseStatusUiState
import java.io.File
import java.io.IOException

/**
 * Holds the application context injected by Koin (androidContext() resolves to the Application,
 * not an Activity), so keeping it for the ViewModel lifetime does not leak UI.
 */
class OcrDependenciesScreenViewModel(
    context: Context,
    private val resourcesApiClient: ArcaeaResourcesApiClient,
    private val remoteResourcesInfoStateHolder: RemoteResourcesInfoStateHolder,
) : ViewModel() {
    companion object {
        private const val STOP_TIME_MILLIS = 5000L
        val sharingStarted = SharingStarted.WhileSubscribed(STOP_TIME_MILLIS)
        private const val LOG_TAG = "OcrDependenciesScreenVM"
    }

    private val logger = Logger.withTag(LOG_TAG)
    private val workManager = WorkManager.getInstance(context.applicationContext)

    private val imageHashesDatabaseBuilderJobInfo =
        workManager
            .getWorkInfosForUniqueWorkFlow(ImageHashesDatabaseBuilderJob.NAME)
            .map { it.firstOrNull() }
            .stateIn(viewModelScope, sharingStarted, null)

    val buildHashesDatabaseButtonEnabled =
        combine(
            ArcaeaResourcesStateHolder.canBuildHashesDatabase,
            imageHashesDatabaseBuilderJobInfo,
        ) { canBuild, workInfo ->
            val enabledByWorkInfo = workInfo == null || workInfo.state.isFinished
            canBuild && enabledByWorkInfo
        }.stateIn(viewModelScope, sharingStarted, true)

    private val imagesHashesDatabaseBuildProgress =
        imageHashesDatabaseBuilderJobInfo
            .map { Progress.fromWorkInfo(it) }
            .stateIn(viewModelScope, sharingStarted, null)

    private val imagesHashesDatabaseStatusDetail =
        MutableStateFlow(ImageHashesDatabaseStatusDetail())
    val imageHashesDatabaseUiState =
        imagesHashesDatabaseBuildProgress
            .combine(imagesHashesDatabaseStatusDetail) { p, s ->
                OcrDependencyImageHashesDatabaseStatusUiState(progress = p, statusDetail = s)
            }.stateIn(viewModelScope, sharingStarted, OcrDependencyImageHashesDatabaseStatusUiState())

    private val _crnnModelUiState = MutableStateFlow(OcrDependencyCrnnModelStatusUiState())
    val crnnModelUiState = _crnnModelUiState.asStateFlow()

    /** Remote metadata of the published directory containing ih.db; items are disabled with an indicator while isFetching. */
    val remoteResourcesInfoState = remoteResourcesInfoStateHolder.state

    data class ImageHashesDatabaseRemoteDownloadUiState(
        val isWorking: Boolean = false,
        val error: String? = null,
    )

    private val _imageHashesDatabaseRemoteDownloadUiState =
        MutableStateFlow(ImageHashesDatabaseRemoteDownloadUiState())
    val imageHashesDatabaseRemoteDownloadUiState =
        _imageHashesDatabaseRemoteDownloadUiState.asStateFlow()

    /** True while a manual ih.db import runs; both import and download write the same files. */
    private val _imageHashesDatabaseImportRunning = MutableStateFlow(false)
    val imageHashesDatabaseImportRunning = _imageHashesDatabaseImportRunning.asStateFlow()

    init {
        reloadAll(context)
    }

    fun refreshRemoteResourcesInfo() = remoteResourcesInfoStateHolder.refresh()

    /** Downloads ih.db into cache, validates it the same way as manual import, then swaps it into place. */
    fun requestImageHashesDatabaseDownload() {
        if (_imageHashesDatabaseRemoteDownloadUiState.value.isWorking) return
        if (_imageHashesDatabaseImportRunning.value) return

        // Set before dispatching: the IO coroutine runs later, and a double tap in that window
        // would otherwise pass the guard twice and race on the shared staging file.
        _imageHashesDatabaseRemoteDownloadUiState.value =
            ImageHashesDatabaseRemoteDownloadUiState(isWorking = true)

        viewModelScope.launch(Dispatchers.IO) {
            val paths = OcrDependencyPaths()
            // Staged next to the destination so the final move stays on one filesystem; the download
            // itself truncates the staging file, so no separate scratch copy is needed.
            val stagingPath = paths.parentDir / "image-hashes.db.staging"

            try {
                if (!mkOcrDependencyParentDirs(paths)) {
                    throw IllegalStateException("Create dependencies parent directory failed!")
                }

                resourcesApiClient.downloadImageHashesDatabase(stagingPath)

                // Same as manual import: the file is only promoted after it opens read-only and builds an ImageHashesDatabase.
                OcrDependencyLoader.imageHashesSQLiteDatabase(stagingPath).use { sqliteDb ->
                    ImageHashesDatabase(sqliteDb)
                }

                // Atomic replace, no pre-delete: a pre-delete would leave ih.db missing if the
                // process died in between.
                atomicReplace(stagingPath, paths.imageHashesDatabaseFile)

                _imageHashesDatabaseRemoteDownloadUiState.value =
                    ImageHashesDatabaseRemoteDownloadUiState()
                reloadImageHashesDatabaseStatusDetailUiState()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.e(e) { "Error downloading image hashes database" }
                _imageHashesDatabaseRemoteDownloadUiState.value =
                    ImageHashesDatabaseRemoteDownloadUiState(error = throwableToErrorText(e))
            } finally {
                if (SystemFileSystem.metadataOrNull(stagingPath) != null) {
                    SystemFileSystem.delete(stagingPath)
                }
            }
        }
    }

    /**
     * Atomically replaces [target] with [source]; both paths must be on the same filesystem.
     *
     * java.nio.file needs API 26+ and cannot be desugared (kotlinx-io probes it via reflection),
     * so on API 24/25 [SystemFileSystem.atomicMove] always throws UnsupportedOperationException.
     * File.renameTo maps to rename(2) on Android: atomic, and it replaces an existing target.
     */
    private fun atomicReplace(
        source: Path,
        target: Path,
    ) {
        try {
            SystemFileSystem.atomicMove(source, target)
        } catch (e: UnsupportedOperationException) {
            if (!File(source.toString()).renameTo(File(target.toString()))) {
                throw IOException("renameTo failed for $source -> $target")
            }
        }
    }

    private fun mkOcrDependencyParentDirs(ocrDependencyPaths: OcrDependencyPaths): Boolean =
        try {
            SystemFileSystem.createDirectories(ocrDependencyPaths.parentDir)
            true
        } catch (e: IOException) {
            logger.w(e) { "Create dependencies parent directory failed!" }
            false
        }

    fun importImageHashesDatabase(
        uri: Uri,
        context: Context,
    ) {
        if (_imageHashesDatabaseRemoteDownloadUiState.value.isWorking) return
        if (_imageHashesDatabaseImportRunning.value) return

        val paths = OcrDependencyPaths()
        if (!mkOcrDependencyParentDirs(paths)) return

        // Set before dispatching, same as [requestImageHashesDatabaseDownload]: both flows write
        // the same files, so the flag must be visible to the next click before any suspension.
        _imageHashesDatabaseImportRunning.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.uriSizeIfTooLarge(uri)?.let { actualSize ->
                    logger.w {
                        "[ImageHashesDatabase] Input file too large, limit is ${Formatter.formatFileSize(
                            context,
                            ArcaeaResourcesApiClient.DEFAULT_MAX_RESOURCE_BYTES,
                        )} " +
                            "while input is ${Formatter.formatFileSize(context, actualSize)}!"
                    }
                    return@launch
                }

                val cacheFile = context.copyToCache(uri, "image_hashes_db_import_temp") ?: return@launch
                // Staged next to the destination so the final move stays on one filesystem.
                val stagingPath = paths.parentDir / "image-hashes.db.staging"
                try {
                    // test if the input is a valid database
                    OcrDependencyLoader.imageHashesSQLiteDatabase(cacheFile).use { sqliteDb ->
                        ImageHashesDatabase(sqliteDb)
                    }

                    // Copy to a staging file first: an interrupted direct copy would truncate the current ih.db.
                    SystemFileSystem.source(cacheFile).buffered().use { src ->
                        SystemFileSystem.sink(stagingPath).buffered().use { dst ->
                            src.transferTo(dst)
                        }
                    }

                    // Atomic replace, no pre-delete: a pre-delete would leave ih.db missing if the
                    // process died in between.
                    atomicReplace(stagingPath, paths.imageHashesDatabaseFile)
                } catch (e: Exception) {
                    if (e is SQLiteException) {
                        logger.w(e) { "Input file doesn't seem like to be a SQLite database" }
                    } else {
                        logger.e(e) { "Error importing image hashes database" }
                    }
                } finally {
                    for (path in listOf(cacheFile, stagingPath)) {
                        if (SystemFileSystem.metadataOrNull(path) != null) {
                            SystemFileSystem.delete(path)
                        }
                    }
                }

                reloadImageHashesDatabaseStatusDetailUiState()
            } finally {
                _imageHashesDatabaseImportRunning.value = false
            }
        }
    }

    private fun reloadImageHashesDatabaseStatusDetailUiState() {
        viewModelScope.launch(Dispatchers.IO) {
            imagesHashesDatabaseStatusDetail.value =
                OcrDependencyStatusBuilder.imageHashesDatabase()
        }
    }

    fun requestImageHashesDatabaseBuild() {
        viewModelScope.launch {
            val workRequest = OneTimeWorkRequestBuilder<ImageHashesDatabaseBuilderJob>().build()

            workManager.enqueueUniqueWork(
                ImageHashesDatabaseBuilderJob.NAME,
                ExistingWorkPolicy.REPLACE,
                workRequest,
            )

            workManager.getWorkInfoByIdFlow(workRequest.id).collect {
                when (it?.state) {
                    WorkInfo.State.SUCCEEDED -> {
                        reloadImageHashesDatabaseStatusDetailUiState()
                    }

                    else -> {}
                }
            }
        }
    }

    private fun reloadCrnnModelStatusDetailUiState(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _crnnModelUiState.value =
                OcrDependencyCrnnModelStatusUiState(
                    statusDetail = OcrDependencyStatusBuilder.crnnModel(context),
                )
        }
    }

    fun reloadAll(context: Context) {
        reloadImageHashesDatabaseStatusDetailUiState()
        reloadCrnnModelStatusDetailUiState(context)
    }
}
