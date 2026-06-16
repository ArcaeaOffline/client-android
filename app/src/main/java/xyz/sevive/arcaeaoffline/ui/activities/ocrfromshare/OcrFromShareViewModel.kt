package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.utils.div
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.asOutputStream
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrOnnxHelper
import xyz.sevive.arcaeaoffline.data.OcrPaths
import xyz.sevive.arcaeaoffline.database.entities.OcrHistory
import xyz.sevive.arcaeaoffline.database.repositories.OcrHistoryRepository
import xyz.sevive.arcaeaoffline.helpers.DeviceOcrHelper
import xyz.sevive.arcaeaoffline.helpers.OcrDependencyLoader
import xyz.sevive.arcaeaoffline.helpers.OcrDependencyStatusBuilder
import xyz.sevive.arcaeaoffline.permissions.storage.SaveBitmapToGallery
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyCrnnModelStatusUiState
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyImageHashesDatabaseStatusUiState
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyKNearestModelStatusUiState

class OcrFromShareViewModel(
    private val playResultRepo: PlayResultRepository,
    private val chartRepo: ChartRepository,
    private val ocrHistoryRepo: OcrHistoryRepository,
) : ViewModel() {
    class OcrDependencyViewersUiState(
        val kNearestModel: OcrDependencyKNearestModelStatusUiState = OcrDependencyKNearestModelStatusUiState(),
        val imageHashesDatabase: OcrDependencyImageHashesDatabaseStatusUiState = OcrDependencyImageHashesDatabaseStatusUiState(),
        val crnnModel: OcrDependencyCrnnModelStatusUiState = OcrDependencyCrnnModelStatusUiState(),
    )

    companion object {
        const val LOG_TAG = "OcrFromShareViewModel"
    }

    private val logger = Logger.withTag(LOG_TAG)

    private val _ocrDependencyViewersUiState = MutableStateFlow(OcrDependencyViewersUiState())
    val ocrDependencyViewersUiState = _ocrDependencyViewersUiState.asStateFlow()

    fun reloadOcrDependencyViewersUiState(context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val kNearest = OcrDependencyStatusBuilder.kNearest()
            val imageHashesDatabase = OcrDependencyStatusBuilder.imageHashesDatabase()
            val crnnModel = OcrDependencyStatusBuilder.crnnModel(context)

            _ocrDependencyViewersUiState.value =
                OcrDependencyViewersUiState(
                    kNearestModel = OcrDependencyKNearestModelStatusUiState(kNearest),
                    imageHashesDatabase = OcrDependencyImageHashesDatabaseStatusUiState(statusDetail = imageHashesDatabase),
                    crnnModel = OcrDependencyCrnnModelStatusUiState(crnnModel),
                )
        }
    }

    private val bitmap = MutableStateFlow<Bitmap?>(null)
    val imageBitmap =
        bitmap.map { it?.asImageBitmap() }.stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            null,
        )

    fun setBitmap(newBitmap: Bitmap) {
        bitmap.value = newBitmap
    }

    private val shareSourceAppPackageName = MutableStateFlow<String?>(null)

    private val _shareSourceAppName = MutableStateFlow<String?>(null)
    val shareSourceAppName = _shareSourceAppName.asStateFlow()

    private val _shareSourceAppIcon = MutableStateFlow<ImageBitmap?>(null)
    val shareSourceAppIcon = _shareSourceAppIcon.asStateFlow()

    fun setShareSourceApp(
        packageName: String?,
        packageManager: PackageManager,
    ) {
        if (packageName == null) {
            shareSourceAppPackageName.value = null
            _shareSourceAppName.value = null
            _shareSourceAppIcon.value = null
            return
        }

        shareSourceAppPackageName.value = packageName
        _shareSourceAppName.value =
            packageName.let {
                try {
                    val info = packageManager.getApplicationInfo(it, 0)
                    packageManager.getApplicationLabel(info).toString()
                } catch (e: PackageManager.NameNotFoundException) {
                    it
                }
            }
        _shareSourceAppIcon.value =
            packageName.let {
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
    val playResult = _playResult.asStateFlow()

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
        if (playResult.value != null) {
            playResultRepo.upsert(playResult.value!!)
            _scoreSaved.value = true
        }
    }

    private val _imageSaved = MutableStateFlow(false)
    val imageSaved = _imageSaved.asStateFlow()

    fun saveImage(context: Context) {
        val bitmap = bitmap.value
        if (bitmap != null && !imageSaved.value) {
            _imageSaved.value =
                SaveBitmapToGallery(bitmap).saveJpg(
                    context,
                    "ocr-from-share-${Instant.now().toEpochMilli()}",
                )
        }
    }

    private val _scoreCached = MutableStateFlow(false)
    val scoreCached = _scoreCached.asStateFlow()

    suspend fun cacheScore(context: Context) {
        val score = playResult.value
        if (score != null) {
            val ocrHistory = OcrHistory.fromArcaeaScore(score, shareSourceAppPackageName.value)
            val id = ocrHistoryRepo.insert(ocrHistory)

            val bitmap = bitmap.value
            if (bitmap != null) {
                val parentDir = OcrPaths().fromShareImageCacheDir
                if (!SystemFileSystem.exists(parentDir)) {
                    SystemFileSystem.createDirectories(parentDir)
                    logger.i { "Created image cache dir" }
                }

                val cacheImagePath = parentDir / "$id.jpg"
                SystemFileSystem.sink(cacheImagePath).buffered().use { sink ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, sink.asOutputStream())
                }
            }

            _scoreCached.value = true
        }
    }

    suspend fun startOcr(
        imageUri: Uri,
        context: Context,
    ) {
        logger.i { "OCR from share request: $imageUri" }

        val ortSession = DeviceOcrOnnxHelper.createOrtSession(context)

        withContext(Dispatchers.IO) {
            try {
                val kNearestModel = OcrDependencyLoader.kNearestModel()
                val imageHashesSQLiteDatabase =
                    OcrDependencyLoader.imageHashesSQLiteDatabase()

                val ocrResult =
                    imageHashesSQLiteDatabase.use { sqliteDb ->
                        val imageHashesDatabase = ImageHashesDatabase(sqliteDb)
                        DeviceOcrHelper.ocrImage(
                            imageUri,
                            kNearestModel,
                            imageHashesDatabase,
                            ortSession = ortSession,
                        )
                    }
                val playResult =
                    DeviceOcrHelper.ocrResultToPlayResult(
                        imageUri,
                        context,
                        ocrResult,
                        fallbackDate = Instant.now(),
                    )

                _playResult.value = playResult
                _exception.value = null

                _chart.value = chartRepo.find(playResult).firstOrNull()
            } catch (e: Exception) {
                _playResult.value = null
                _exception.value = e

                logger.e(e) { "Error occurred during OCR" }
            } finally {
                ortSession.close()
            }
        }
    }
}
