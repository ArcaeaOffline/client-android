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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.core.Progress
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabase
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.helpers.ArcaeaResourcesStateHolder
import xyz.sevive.arcaeaoffline.helpers.ImageHashesDatabaseStatusDetail
import xyz.sevive.arcaeaoffline.helpers.OcrDependencyLoader
import xyz.sevive.arcaeaoffline.helpers.OcrDependencyStatusBuilder
import xyz.sevive.arcaeaoffline.helpers.context.copyToCache
import xyz.sevive.arcaeaoffline.helpers.context.getFileSize
import xyz.sevive.arcaeaoffline.helpers.fromWorkInfo
import xyz.sevive.arcaeaoffline.jobs.ImageHashesDatabaseBuilderJob
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyCrnnModelStatusUiState
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyImageHashesDatabaseStatusUiState
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKNearestModelStatusUiState
import java.io.IOException

class OcrDependenciesScreenViewModel(
    context: Context,
) : ViewModel() {
    companion object {
        private const val STOP_TIME_MILLIS = 5000L
        val sharingStarted = SharingStarted.WhileSubscribed(STOP_TIME_MILLIS)
        private const val LOG_TAG = "OcrDependenciesScreenVM"
    }

    private val logger = Logger.withTag(LOG_TAG)
    private val workManager = WorkManager.getInstance(context.applicationContext)

    private val _kNearestModelUiState =
        MutableStateFlow(OcrDependencyKNearestModelStatusUiState())
    val kNearestModelUiState = _kNearestModelUiState.asStateFlow()

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

    init {
        reloadAll(context)
    }

    private fun mkOcrDependencyParentDirs(ocrDependencyPaths: OcrDependencyPaths): Boolean =
        try {
            SystemFileSystem.createDirectories(ocrDependencyPaths.parentDir)
            true
        } catch (e: IOException) {
            logger.w(e) { "Create dependencies parent directory failed!" }
            false
        }

    private fun isFileTooLarge(
        uri: Uri,
        context: Context,
        limit: Long = 20 * 1024 * 1024,
        logName: String? = null,
    ): Boolean {
        val fileSize = context.getFileSize(uri) ?: return false
        if (fileSize <= limit) return false

        logger.w {
            buildString {
                logName?.let { append("[$logName] ") }
                append("Input file too large, ")
                append("limit is ${Formatter.formatFileSize(context, limit)} ")
                append("while input is ${Formatter.formatFileSize(context, fileSize)}!")
            }
        }
        return true
    }

    fun importKNearestModel(
        uri: Uri,
        context: Context,
    ) {
        val paths = OcrDependencyPaths()
        if (!mkOcrDependencyParentDirs(paths)) return

        viewModelScope.launch(Dispatchers.IO) {
            if (isFileTooLarge(uri, context, logName = "KNearest")) return@launch

            val cacheFile = context.copyToCache(uri, "knearest_model_import_temp") ?: return@launch
            try {
                KNearest.load(cacheFile.toString())
                SystemFileSystem.source(cacheFile).buffered().use { src ->
                    SystemFileSystem.sink(paths.knnModelFile).buffered().use { dst ->
                        src.transferTo(dst)
                    }
                }
            } catch (e: Exception) {
                logger.e(e) { "Error importing KNearest model" }
            } finally {
                SystemFileSystem.delete(cacheFile)
            }

            reloadKNearestModelStatusDetailUiState()
        }
    }

    fun importImageHashesDatabase(
        uri: Uri,
        context: Context,
    ) {
        val paths = OcrDependencyPaths()
        if (!mkOcrDependencyParentDirs(paths)) return

        viewModelScope.launch(Dispatchers.IO) {
            if (isFileTooLarge(uri, context, logName = "ImageHashesDatabase")) return@launch

            val cacheFile = context.copyToCache(uri, "image_hashes_db_import_temp") ?: return@launch
            try {
                // test if the input is a valid database
                OcrDependencyLoader.imageHashesSQLiteDatabase(cacheFile).use { sqliteDb ->
                    ImageHashesDatabase(sqliteDb)
                }

                SystemFileSystem.source(cacheFile).buffered().use { src ->
                    SystemFileSystem.sink(paths.imageHashesDatabaseFile).buffered().use { dst ->
                        src.transferTo(dst)
                    }
                }
            } catch (e: Exception) {
                if (e is SQLiteException) {
                    logger.w(e) { "Input file doesn't seem like to be a SQLite database" }
                } else {
                    logger.e(e) { "Error importing image hashes database" }
                }
            } finally {
                SystemFileSystem.delete(cacheFile)
            }

            reloadImageHashesDatabaseStatusDetailUiState()
        }
    }

    private fun reloadKNearestModelStatusDetailUiState() {
        viewModelScope.launch(Dispatchers.IO) {
            _kNearestModelUiState.value =
                OcrDependencyKNearestModelStatusUiState(OcrDependencyStatusBuilder.kNearest())
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
        reloadKNearestModelStatusDetailUiState()
        reloadImageHashesDatabaseStatusDetailUiState()
        reloadCrnnModelStatusDetailUiState(context)
    }
}
