package xyz.sevive.arcaeaoffline.ui.activities.ocrfromshare

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.common.ImageWriteException
import de.stefan_oltmann.kim.input.ByteArrayByteReader
import de.stefan_oltmann.kim.model.MediaFormat
import de.stefan_oltmann.kim.model.MetadataUpdate
import io.github.vinceglb.filekit.utils.div
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import kotlinx.io.asOutputStream
import kotlinx.io.buffered
import kotlinx.io.files.SystemFileSystem
import xyz.sevive.arcaeaoffline.R
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyWithSongRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrOnnxHelper
import xyz.sevive.arcaeaoffline.core.validation.PlayResultValidator
import xyz.sevive.arcaeaoffline.data.OcrPaths
import xyz.sevive.arcaeaoffline.database.entities.OcrHistory
import xyz.sevive.arcaeaoffline.database.repositories.OcrHistoryRepository
import xyz.sevive.arcaeaoffline.helpers.DeviceOcrHelper
import xyz.sevive.arcaeaoffline.helpers.OcrDependencyLoader
import xyz.sevive.arcaeaoffline.helpers.OcrDependencyStatusBuilder
import xyz.sevive.arcaeaoffline.permissions.storage.SaveImageToGallery
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyCrnnModelStatusUiState
import xyz.sevive.arcaeaoffline.ui.components.ocr.OcrDependencyImageHashesDatabaseStatusUiState
import xyz.sevive.arcaeaoffline.ui.helpers.DifficultyWithSongDisplay
import xyz.sevive.arcaeaoffline.ui.helpers.UiEvent
import xyz.sevive.arcaeaoffline.ui.helpers.UiEventChannel
import java.io.IOException
import kotlin.concurrent.Volatile
import kotlin.time.Clock
import kotlin.time.Instant

class OcrFromShareViewModel(
    private val appContext: Context,
    private val playResultRepo: PlayResultRepository,
    private val ocrHistoryRepo: OcrHistoryRepository,
    private val difficultyWithSongRepo: DifficultyWithSongRepository,
    private val chartInfoRepo: ChartInfoRepository,
) : ViewModel() {
    class OcrDependencyViewersUiState(
        val imageHashesDatabase: OcrDependencyImageHashesDatabaseStatusUiState = OcrDependencyImageHashesDatabaseStatusUiState(),
        val crnnModel: OcrDependencyCrnnModelStatusUiState = OcrDependencyCrnnModelStatusUiState(),
    )

    /** Recognition outcome: waiting while OCR runs, failed with its error, or done with the result. */
    sealed interface OcrResultState {
        data object Waiting : OcrResultState

        data class Failed(
            val exception: Exception,
        ) : OcrResultState

        data class Done(
            val playResult: PlayResult,
        ) : OcrResultState
    }

    data class ShareSourceUiState(
        val name: String? = null,
        val icon: ImageBitmap? = null,
    )

    data class PlayResultSaveState(
        val saved: Boolean = false,
        val cached: Boolean = false,
    )

    /** The share as delivered by the Activity, captured once at [onImageShared] and reused by retries. */
    data class ShareRequest(
        val uri: Uri,
        val sharedAt: Instant,
    )

    /** The image this share session is about, captured once at [onImageShared]. */
    @Suppress("ArrayInDataClass")
    data class SharedImage(
        val uri: Uri,
        val sharedAt: Instant,
        val sourceTakenAt: Instant?,
        val bytes: ByteArray,
    )

    companion object {
        const val LOG_TAG = "OcrFromShareViewModel"
    }

    private val logger = Logger.withTag(LOG_TAG)

    private val eventChannel = UiEventChannel()
    val events = eventChannel.events

    private val _ocrDependencyViewersUiState = MutableStateFlow<OcrDependencyViewersUiState?>(null)
    val ocrDependencyViewersUiState = _ocrDependencyViewersUiState.asStateFlow()

    init {
        reloadOcrDependencyViewersUiState()
    }

    fun reloadOcrDependencyViewersUiState() {
        viewModelScope.launch(Dispatchers.IO) {
            val imageHashesDatabase = OcrDependencyStatusBuilder.imageHashesDatabase()
            val crnnModel = OcrDependencyStatusBuilder.crnnModel(appContext)

            _ocrDependencyViewersUiState.value =
                OcrDependencyViewersUiState(
                    imageHashesDatabase = OcrDependencyImageHashesDatabaseStatusUiState(statusDetail = imageHashesDatabase),
                    crnnModel = OcrDependencyCrnnModelStatusUiState(crnnModel),
                )
        }
    }

    private val shareSourceAppPackageName = MutableStateFlow<String?>(null)

    private val _shareSourceUiState = MutableStateFlow(ShareSourceUiState())
    val shareSourceUiState = _shareSourceUiState.asStateFlow()

    fun setShareSourceApp(
        packageName: String?,
        packageManager: PackageManager,
    ) {
        if (packageName == null) {
            logger.w { "Share app packageName is null" }
            shareSourceAppPackageName.value = null
            _shareSourceUiState.value = ShareSourceUiState()
            return
        }

        shareSourceAppPackageName.value = packageName
        _shareSourceUiState.value =
            ShareSourceUiState(
                name =
                    packageName.let {
                        try {
                            val info = packageManager.getApplicationInfo(it, 0)
                            packageManager.getApplicationLabel(info).toString()
                        } catch (e: PackageManager.NameNotFoundException) {
                            logger.w(e) { "Failed to resolve share app name" }
                            it
                        }
                    },
                icon =
                    packageName.let {
                        try {
                            packageManager.getApplicationIcon(it).toBitmap().asImageBitmap()
                        } catch (e: PackageManager.NameNotFoundException) {
                            logger.w(e) { "Failed to resolve share app icon" }
                            null
                        }
                    },
            )
    }

    private val _ocrResultState = MutableStateFlow<OcrResultState>(OcrResultState.Waiting)
    val ocrResultState = _ocrResultState.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val display =
        _ocrResultState
            .flatMapLatest { state ->
                val playResult = (state as? OcrResultState.Done)?.playResult
                if (playResult == null) {
                    flowOf(null)
                } else {
                    combine(
                        difficultyWithSongRepo.find(playResult.songId, playResult.ratingClass),
                        chartInfoRepo.find(playResult.songId, playResult.ratingClass),
                    ) { dws, info ->
                        dws?.let { DifficultyWithSongDisplay(it, info) }
                    }
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val warnings =
        _ocrResultState
            .flatMapLatest { state ->
                val playResult = (state as? OcrResultState.Done)?.playResult
                if (playResult == null) {
                    flowOf(emptyList())
                } else {
                    display.map { d ->
                        PlayResultValidator.validate(playResult = playResult, chartInfo = d?.chartInfo)
                    }
                }
            }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    private var sharedImage: SharedImage? = null

    private val _shareRequest = MutableStateFlow<ShareRequest?>(null)
    val shareRequest = _shareRequest.asStateFlow()

    /** Single entry for Activity */
    fun onImageShared(uri: Uri) {
        // The Activity re-delivers the share intent on recreation, so holding a flag here
        // to prevent dropping the user's edits and the saved state.
        if (_shareRequest.value?.uri == uri) return

        val request = ShareRequest(uri, Clock.System.now())
        _shareRequest.value = request
        viewModelScope.launch { readImageAndStartOcr(request) }
    }

    private suspend fun readImageAndStartOcr(request: ShareRequest) {
        _ocrResultState.value = OcrResultState.Waiting

        try {
            withContext(Dispatchers.IO) {
                val bytes =
                    appContext.contentResolver.openInputStream(request.uri)?.use { it.readBytes() }
                        ?: throw IOException("Cannot open shared image stream")

                val sourceTakenAt =
                    runCatching {
                        DeviceOcrHelper.readImageDateFromExif(ByteArrayByteReader(bytes))
                    }.getOrNull()

                sharedImage = SharedImage(request.uri, request.sharedAt, sourceTakenAt, bytes)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _ocrResultState.value = OcrResultState.Failed(Exception("Error reading image", e))
            return
        }

        startOcr(request.uri)
    }

    fun retryOcr() {
        val request = _shareRequest.value ?: return
        viewModelScope.launch { readImageAndStartOcr(request) }
    }

    fun setPlayResult(playResult: PlayResult) {
        _ocrResultState.value = OcrResultState.Done(playResult)
    }

    private val _playResultSaveState = MutableStateFlow(PlayResultSaveState())
    val playResultSaveState = _playResultSaveState.asStateFlow()

    @Volatile
    private var playResultSaveInFlight = false

    fun savePlayResult() {
        if (playResultSaveInFlight || playResultSaveState.value.saved) return
        val playResult = (_ocrResultState.value as? OcrResultState.Done)?.playResult ?: return
        playResultSaveInFlight = true

        viewModelScope.launch {
            try {
                playResultRepo.upsert(playResult)
                _playResultSaveState.value = _playResultSaveState.value.copy(saved = true)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.e(e) { "Error occurred while saving play result" }
                eventChannel.send(
                    UiEvent.StringRes(R.string.ocr_from_share_play_result_save_failed, listOf(e.toString())),
                )
            } finally {
                playResultSaveInFlight = false
            }
        }
    }

    private val _imageSaved = MutableStateFlow(false)
    val imageSaved = _imageSaved.asStateFlow()

    @Volatile
    private var imageSaveInFlight = false

    fun saveImage() {
        val shared = sharedImage ?: return
        if (imageSaveInFlight || imageSaved.value) return
        imageSaveInFlight = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val dateTaken = shared.sourceTakenAt ?: shared.sharedAt
                val fileName =
                    "ocr-from-share-" +
                        imageFileDateFormat.format(dateTaken.toLocalDateTime(TimeZone.currentSystemDefault())) +
                        "." + imageFileExtension(shared.bytes)

                val bytes =
                    if (shared.sourceTakenAt != null) {
                        shared.bytes
                    } else {
                        try {
                            Kim.update(
                                shared.bytes,
                                MetadataUpdate.TakenDate(shared.sharedAt.toEpochMilliseconds()),
                            )
                        } catch (e: ImageWriteException) {
                            logger.w(e) { "Writing EXIF taken date failed, saving without it" }
                            shared.bytes
                        }
                    }

                SaveImageToGallery.saveImageBytes(
                    appContext,
                    fileName,
                    bytes,
                    dateTaken.toEpochMilliseconds(),
                    imageFileMimeType(shared.bytes),
                )
                _imageSaved.value = true
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.e(e) { "Error occurred while saving image" }
                eventChannel.send(
                    UiEvent.StringRes(R.string.ocr_from_share_image_save_failed, listOf(e.toString())),
                )
            } finally {
                imageSaveInFlight = false
            }
        }
    }

    @Suppress("unused")
    suspend fun cachePlayResult() {
        val playResult = (_ocrResultState.value as? OcrResultState.Done)?.playResult ?: return
        val ocrHistory =
            OcrHistory.fromArcaeaScore(
                playResult = playResult,
                sourcePackageName = shareSourceAppPackageName.value,
                storeDate = Clock.System.now(),
            )
        val id = ocrHistoryRepo.insert(ocrHistory)

        sharedImage?.let { shared ->
            val parentDir = OcrPaths().fromShareImageCacheDir
            if (!SystemFileSystem.exists(parentDir)) {
                SystemFileSystem.createDirectories(parentDir)
                logger.i { "Created image cache dir" }
            }

            val cacheImagePath = parentDir / "$id.${imageFileExtension(shared.bytes)}"
            SystemFileSystem.sink(cacheImagePath).buffered().use { it.asOutputStream().write(shared.bytes) }
        }

        _playResultSaveState.value = _playResultSaveState.value.copy(cached = true)
    }

    private suspend fun startOcr(uri: Uri) {
        logger.i { "OCR from share request: $uri" }

        withContext(Dispatchers.IO) {
            try {
                DeviceOcrOnnxHelper.createOrtSession(appContext).use { ortSession ->
                    val imageHashesSQLiteDatabase =
                        OcrDependencyLoader.imageHashesSQLiteDatabase()

                    val ocrResult =
                        imageHashesSQLiteDatabase.use { sqliteDb ->
                            val imageHashesDatabase = ImageHashesDatabase(sqliteDb)
                            DeviceOcrHelper.ocrImage(
                                uri,
                                imageHashesDatabase,
                                ortSession = ortSession,
                            )
                        }
                    val playResult =
                        DeviceOcrHelper.ocrResultToPlayResult(
                            uri,
                            appContext,
                            ocrResult,
                            fallbackDate = Clock.System.now(),
                        )

                    _ocrResultState.value = OcrResultState.Done(playResult)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _ocrResultState.value = OcrResultState.Failed(e)

                logger.e(e) { "Error occurred during OCR" }
            }
        }
    }
}

private val imageFileDateFormat =
    LocalDateTime.Format {
        year()
        monthNumber()
        day()
        char('_')
        hour()
        minute()
        second()
    }

private fun imageFileExtension(bytes: ByteArray): String = MediaFormat.detect(bytes)?.fileNameExtensions?.firstOrNull() ?: "jpg"

private fun imageFileMimeType(bytes: ByteArray): String = MediaFormat.detect(bytes)?.mimeType ?: "image/jpeg"
