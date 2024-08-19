package xyz.sevive.arcaeaoffline.ui.ocr.dependencies

import android.content.Context
import android.database.sqlite.SQLiteException
import android.net.Uri
import android.text.format.Formatter
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.ArcaeaOfflineApplication
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabase
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabaseBuilderJob
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabaseStatusDetail
import xyz.sevive.arcaeaoffline.core.ocr.OcrDependencyLoader
import xyz.sevive.arcaeaoffline.core.ocr.OcrDependencyStatusBuilder
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.helpers.context.copyToCache
import xyz.sevive.arcaeaoffline.helpers.context.getFileSize
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyCrnnModelStatusUiState
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyImageHashesDatabaseStatusUiState
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKNearestModelStatusUiState
import java.util.UUID

class OcrDependenciesScreenViewModel(application: ArcaeaOfflineApplication) : ViewModel() {
    companion object {
        private const val STOP_TIME_MILLIS = 5000L
        val sharingStarted = SharingStarted.WhileSubscribed(STOP_TIME_MILLIS)
        private const val LOG_TAG = "OcrDependenciesScreenVM"
    }

    private val _kNearestModelStatusUiState =
        MutableStateFlow(OcrDependencyKNearestModelStatusUiState())
    val kNearestModelUiState = _kNearestModelStatusUiState.asStateFlow()

    private val _imagesHashesDatabaseBuildProgress = MutableStateFlow<Pair<Int, Int>?>(null)
    private val _imagesHashesDatabaseStatusDetail =
        MutableStateFlow(ImageHashesDatabaseStatusDetail())
    val imageHashesDatabaseUiState =
        _imagesHashesDatabaseBuildProgress.combine(_imagesHashesDatabaseStatusDetail) { p, s ->
            OcrDependencyImageHashesDatabaseStatusUiState(progress = p, statusDetail = s)
        }.stateIn(viewModelScope, sharingStarted, OcrDependencyImageHashesDatabaseStatusUiState())

    private var imageHashesDatabaseBuilderJobProgressListener: Job? = null

    private val _crnnModelUiState = MutableStateFlow(OcrDependencyCrnnModelStatusUiState())
    val crnnModelUiState = _crnnModelUiState.asStateFlow()

    init {
        reloadAll(application)
    }

    private fun mkOcrDependencyParentDirs(ocrDependencyPaths: OcrDependencyPaths): Boolean {
        if (ocrDependencyPaths.parentDir.exists()) return true

        val result = ocrDependencyPaths.parentDir.mkdirs()
        if (!result) Log.w(LOG_TAG, "Create dependencies parent directory failed!")
        return result
    }

    private fun isFileToLarge(
        uri: Uri, context: Context, limit: Long = 20 * 1024 * 1024, logName: String? = null
    ): Boolean {
        val fileSize = context.getFileSize(uri) ?: return false
        if (fileSize <= limit) return false

        Log.w(LOG_TAG, buildString {
            logName?.let { append("[$logName] ") }
            append("Input file too large, ")
            append("limit is ${Formatter.formatFileSize(context, limit)} ")
            append("while input is ${Formatter.formatFileSize(context, fileSize)}!")
        })
        return true
    }

    fun importKNearestModel(uri: Uri, context: Context) {
        val paths = OcrDependencyPaths(context)
        if (!mkOcrDependencyParentDirs(paths)) return

        viewModelScope.launch(Dispatchers.IO) {
            if (isFileToLarge(uri, context, logName = "KNearest")) return@launch

            val cacheFile = context.copyToCache(uri, "knearest_model_import_temp") ?: return@launch
            try {
                KNearest.load(cacheFile.absolutePath)
                FileUtils.copyFile(cacheFile, paths.knnModelFile)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Error importing KNearest model", e)
            } finally {
                cacheFile.delete()
            }

            reloadKNearestModelStatusDetailUiState(context)
        }
    }

    fun importImageHashesDatabase(uri: Uri, context: Context) {
        val paths = OcrDependencyPaths(context)
        if (!mkOcrDependencyParentDirs(paths)) return

        viewModelScope.launch(Dispatchers.IO) {
            if (isFileToLarge(uri, context, logName = "ImageHashesDatabase")) return@launch

            val cacheFile = context.copyToCache(uri, "image_hashes_db_import_temp") ?: return@launch
            try {
                // test if the input is a valid database
                OcrDependencyLoader.imageHashesSQLiteDatabase(cacheFile).use { sqliteDb ->
                    ImageHashesDatabase(sqliteDb)
                }

                FileUtils.copyFile(cacheFile, paths.imageHashesDatabaseFile)
            } catch (e: Exception) {
                if (e is SQLiteException) {
                    Log.w(LOG_TAG, "Input file doesn't seem like to be a SQLite database", e)
                } else {
                    Log.e(LOG_TAG, "Error importing image hashes database", e)
                }
            } finally {
                cacheFile.delete()
            }

            reloadImageHashesDatabaseStatusDetailUiState(context)
        }
    }

    private fun reloadKNearestModelStatusDetailUiState(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _kNearestModelStatusUiState.value =
                OcrDependencyKNearestModelStatusUiState(OcrDependencyStatusBuilder.kNearest(context))
        }
    }

    private fun reloadImageHashesDatabaseStatusDetailUiState(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _imagesHashesDatabaseStatusDetail.value =
                OcrDependencyStatusBuilder.imageHashesDatabase(context)
        }
    }

    private fun startImageHashesDatabaseBuilderJobProgressListener(context: Context, workId: UUID) {
        imageHashesDatabaseBuilderJobProgressListener?.cancel()
        imageHashesDatabaseBuilderJobProgressListener = viewModelScope.launch {
            WorkManager.getInstance(context).getWorkInfoByIdFlow(workId).collect {
                val info = it ?: return@collect

                val progress = info.progress.getInt(ImageHashesDatabaseBuilderJob.KEY_PROGRESS, -1)
                val total =
                    info.progress.getInt(ImageHashesDatabaseBuilderJob.KEY_PROGRESS_TOTAL, -1)

                _imagesHashesDatabaseBuildProgress.value =
                    if (progress == -1) null else progress to total

                if (info.state.isFinished) {
                    _imagesHashesDatabaseBuildProgress.value = null
                    reloadImageHashesDatabaseStatusDetailUiState(context)
                    this.cancel()
                }
            }
        }
    }

    fun requestImageHashesDatabaseBuild(context: Context) {
        viewModelScope.launch {
            val workRequest = OneTimeWorkRequestBuilder<ImageHashesDatabaseBuilderJob>().build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                ImageHashesDatabaseBuilderJob.NAME, ExistingWorkPolicy.REPLACE, workRequest
            )
            startImageHashesDatabaseBuilderJobProgressListener(context, workRequest.id)
        }
    }

    private fun reloadCrnnModelStatusDetailUiState(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _crnnModelUiState.value = OcrDependencyCrnnModelStatusUiState(
                statusDetail = OcrDependencyStatusBuilder.crnnModel(context)
            )
        }
    }

    fun reloadAll(context: Context) {
        reloadKNearestModelStatusDetailUiState(context)
        reloadImageHashesDatabaseStatusDetailUiState(context)
        reloadCrnnModelStatusDetailUiState(context)
    }
}
