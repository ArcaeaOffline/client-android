package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabase
import xyz.sevive.arcaeaoffline.core.ocr.OcrDependencyLoader
import xyz.sevive.arcaeaoffline.core.ocr.OcrDependencyStatusBuilder
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrOnnxHelper
import xyz.sevive.arcaeaoffline.data.OcrPaths
import xyz.sevive.arcaeaoffline.database.entities.OcrHistory
import xyz.sevive.arcaeaoffline.helpers.DeviceOcrHelper
import xyz.sevive.arcaeaoffline.permissions.storage.SaveBitmapToGallery
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyCrnnModelStatusUiState
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyImageHashesDatabaseStatusUiState
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKNearestModelStatusUiState
import xyz.sevive.arcaeaoffline.ui.containers.AppDatabaseRepositoryContainer
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer
import java.io.File


class OcrFromShareViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer,
    private val appDatabaseRepositoryContainer: AppDatabaseRepositoryContainer,
) : ViewModel() {
    class OcrDependencyViewersUiState(
        val kNearestModel: OcrDependencyKNearestModelStatusUiState = OcrDependencyKNearestModelStatusUiState(),
        val imageHashesDatabase: OcrDependencyImageHashesDatabaseStatusUiState = OcrDependencyImageHashesDatabaseStatusUiState(),
        val crnnModel: OcrDependencyCrnnModelStatusUiState = OcrDependencyCrnnModelStatusUiState(),
    )

    private val _ocrDependencyViewersUiState = MutableStateFlow(OcrDependencyViewersUiState())
    val ocrDependencyViewersUiState = _ocrDependencyViewersUiState.asStateFlow()

    fun reloadOcrDependencyViewersUiState(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val kNearest = OcrDependencyStatusBuilder.kNearest(context)
            val imageHashesDatabase = OcrDependencyStatusBuilder.imageHashesDatabase(context)
            val crnnModel = OcrDependencyStatusBuilder.crnnModel(context)

            _ocrDependencyViewersUiState.value = OcrDependencyViewersUiState(
                kNearestModel = OcrDependencyKNearestModelStatusUiState(kNearest),
                imageHashesDatabase = OcrDependencyImageHashesDatabaseStatusUiState(statusDetail = imageHashesDatabase),
                crnnModel = OcrDependencyCrnnModelStatusUiState(crnnModel),
            )
        }
    }

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    private val bitmap = _bitmap.asStateFlow()
    val imageBitmap = bitmap.map { it?.asImageBitmap() }.stateIn(
        viewModelScope, SharingStarted.Lazily, null
    )

    fun setBitmap(bitmap: Bitmap) {
        _bitmap.value = bitmap
    }

    private val _shareSourceAppPackageName = MutableStateFlow<String?>(null)
    private val shareSourceAppPackageName = _shareSourceAppPackageName.asStateFlow()

    private val _shareSourceAppName = MutableStateFlow<String?>(null)
    val shareSourceAppName = _shareSourceAppName.asStateFlow()

    private val _shareSourceAppIcon = MutableStateFlow<ImageBitmap?>(null)
    val shareSourceAppIcon = _shareSourceAppIcon.asStateFlow()

    fun setShareSourceApp(packageName: String?, packageManager: PackageManager) {
        if (packageName == null) {
            _shareSourceAppPackageName.value = null
            _shareSourceAppName.value = null
            _shareSourceAppIcon.value = null
            return
        }

        _shareSourceAppPackageName.value = packageName
        _shareSourceAppName.value = packageName.let {
            try {
                val info = packageManager.getApplicationInfo(it, 0)
                packageManager.getApplicationLabel(info).toString()
            } catch (e: PackageManager.NameNotFoundException) {
                it
            }
        }
        _shareSourceAppIcon.value = packageName.let {
            try {
                packageManager.getApplicationIcon(it).toBitmap().asImageBitmap()
            } catch (e: PackageManager.NameNotFoundException) {
                null
            }
        }
    }

    private val _chart = MutableStateFlow<Chart?>(null)
    val chart = _chart.asStateFlow()

    private val _playResult = MutableStateFlow<PlayResult?>(null)
    val score = _playResult.asStateFlow()

    private val _exception = MutableStateFlow<Exception?>(null)
    val exception = _exception.asStateFlow()

    fun setScore(playResult: PlayResult?) {
        _playResult.value = playResult
    }

    fun setException(e: Exception) {
        _exception.value = e
    }

    private val _scoreSaved = MutableStateFlow(false)
    val scoreSaved = _scoreSaved.asStateFlow()

    suspend fun saveScore() {
        if (score.value != null) {
            repositoryContainer.playResultRepo.upsert(score.value!!)
            _scoreSaved.value = true
        }
    }

    private val _imageSaved = MutableStateFlow(false)
    val imageSaved = _imageSaved.asStateFlow()

    fun saveImage(context: Context) {
        val bitmap = bitmap.value
        if (bitmap != null && !imageSaved.value) {
            _imageSaved.value = SaveBitmapToGallery(bitmap).saveJpg(
                context, "ocr-from-share-${Instant.now().toEpochMilli()}"
            )
        }
    }

    private val _scoreCached = MutableStateFlow(false)
    val scoreCached = _scoreCached.asStateFlow()

    suspend fun cacheScore(context: Context) {
        val score = score.value
        if (score != null) {
            val ocrHistory = OcrHistory.fromArcaeaScore(score, shareSourceAppPackageName.value)
            val id = appDatabaseRepositoryContainer.ocrHistoryRepo.insert(ocrHistory)

            val bitmap = bitmap.value
            if (bitmap != null) {
                val parentDir = OcrPaths(context).fromShareImageCacheDir
                if (!parentDir.exists()) {
                    Log.i(TAG, "Creating image cache dir, success: ${parentDir.mkdirs()}")
                }

                val cacheImageFile = File(parentDir, "$id.jpg")
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, cacheImageFile.outputStream())
            }

            _scoreCached.value = true
        }
    }

    suspend fun startOcr(imageUri: Uri, context: Context) {
        Log.i(TAG, "OCR from share request: $imageUri")

        val ortSession = DeviceOcrOnnxHelper.createOrtSession(context)

        withContext(Dispatchers.IO) {
            try {
                val kNearestModel = OcrDependencyLoader.kNearestModel(context)
                val imageHashesSQLiteDatabase =
                    OcrDependencyLoader.imageHashesSQLiteDatabase(context)

                val ocrResult = imageHashesSQLiteDatabase.use { sqliteDb ->
                    val imageHashesDatabase = ImageHashesDatabase(sqliteDb)
                    DeviceOcrHelper.ocrImage(
                        imageUri,
                        context,
                        kNearestModel,
                        imageHashesDatabase,
                        ortSession = ortSession
                    )
                }
                val playResult = DeviceOcrHelper.ocrResultToPlayResult(
                    imageUri,
                    context,
                    ocrResult,
                    fallbackDate = Instant.now(),
                )

                _playResult.value = playResult
                _exception.value = null

                _chart.value = repositoryContainer.chartRepo.find(playResult).firstOrNull()
            } catch (e: Exception) {
                _playResult.value = null
                _exception.value = e

                Log.e(TAG, "Error occurred during OCR", e)
            } finally {
                ortSession.close()
            }
        }
    }

    companion object {
        const val TAG = "OcrFromShareViewModel"
    }
}
