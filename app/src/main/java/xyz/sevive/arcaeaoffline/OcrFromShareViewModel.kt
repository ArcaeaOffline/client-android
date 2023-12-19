package xyz.sevive.arcaeaoffline

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.threeten.bp.Instant
import xyz.sevive.arcaeaoffline.core.database.entities.Chart
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.core.database.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.core.helpers.DeviceOcrHelper
import xyz.sevive.arcaeaoffline.database.AppDatabase
import xyz.sevive.arcaeaoffline.database.entities.OcrHistory
import xyz.sevive.arcaeaoffline.ui.containers.ArcaeaOfflineDatabaseRepositoryContainer


class OcrFromShareViewModel(
    private val repositoryContainer: ArcaeaOfflineDatabaseRepositoryContainer,
    private val appDatabase: AppDatabase,
) : ViewModel() {
    private val _imageBitmap = MutableStateFlow<ImageBitmap?>(null)
    val imageBitmap = _imageBitmap.asStateFlow()

    fun setImageBitmap(bitmap: Bitmap) {
        _imageBitmap.value = bitmap.asImageBitmap()
    }

    private val _chart = MutableStateFlow<Chart?>(null)
    val chart = _chart.asStateFlow()

    private val _score = MutableStateFlow<Score?>(null)
    val score = _score.asStateFlow()

    private val _exception = MutableStateFlow<Exception?>(null)
    val exception = _exception.asStateFlow()

    fun setScore(score: Score?) {
        _score.value = score
    }

    fun setException(e: Exception) {
        _exception.value = e
    }

    private suspend fun setChart(songId: String, ratingClass: Int) {
        val chart = repositoryContainer.chartRepository.find(songId, ratingClass).firstOrNull()
        if (chart != null) {
            _chart.value = chart
            return
        }

        val song = repositoryContainer.songRepository.find(songId).firstOrNull()
        val difficulty =
            repositoryContainer.difficultyRepository.find(songId, ratingClass).firstOrNull()

        if (song != null && difficulty != null) {
            _chart.value = ChartFactory.getChart(song, difficulty)
            return
        }

        _chart.value = null
    }

    private val _scoreSaved = MutableStateFlow(false)
    val scoreSaved = _scoreSaved.asStateFlow()

    suspend fun saveScore() {
        if (score.value != null) {
            repositoryContainer.scoreRepository.upsert(score.value!!)
            _scoreSaved.value = true
        }
    }

    private val _scoreCached = MutableStateFlow(false)
    val scoreCached = _scoreCached.asStateFlow()

    suspend fun cacheScore(sourcePackageName: String? = null) {
        val score = score.value

        if (score != null) {
            val ocrHistory = OcrHistory.fromArcaeaScore(score, sourcePackageName)
            appDatabase.ocrHistoryDao().insert(ocrHistory)
            _scoreCached.value = true
        }
    }

    fun startOcr(imageUri: Uri, context: Context) {
        try {
            val ocrResult = DeviceOcrHelper.ocrImage(imageUri, context)
            val score = DeviceOcrHelper.ocrResultToScore(
                imageUri,
                context,
                ocrResult,
                fallbackDate = Instant.now().epochSecond,
            )

            _score.value = score
            _exception.value = null

            runBlocking {
                setChart(ocrResult.songId, ocrResult.ratingClass)
            }
        } catch (e: Exception) {
            _score.value = null
            _exception.value = e

            Log.e("OCR", "Error occurred during OCR", e)
        }
    }
}
