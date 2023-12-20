package xyz.sevive.arcaeaoffline.core.helpers

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.core.ocr.ImagePhashDatabase
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths

object OcrDependencyHelper {
    private const val LOG_TAG = "OcrDependencyHelper"

    private val _kNearestModel = MutableStateFlow<KNearest?>(null)
    val kNearestModel = _kNearestModel.asStateFlow()

    private val _kNearestModelException = MutableStateFlow<Exception?>(null)
    val kNearestModelException = _kNearestModelException.asStateFlow()

    private val _imagePhashDatabase = MutableStateFlow<ImagePhashDatabase?>(null)
    val imagePhashDatabase = _imagePhashDatabase.asStateFlow()

    private val _imagePhashDatabaseException = MutableStateFlow<Exception?>(null)
    val imagePhashDatabaseException = _imagePhashDatabaseException.asStateFlow()

    private fun loadKNearestModel(filePath: String) {
        try {
            _kNearestModel.value = KNearest.load(filePath)
            _kNearestModelException.value = null
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error loading KNearest model", e)
            _kNearestModelException.value = e
            _kNearestModel.value = null
        }
    }

    private fun loadImagePhashDatabase(filePath: String) {
        try {
            _imagePhashDatabase.value = ImagePhashDatabase(filePath)
            _imagePhashDatabaseException.value = null
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error loading image phash database", e)
            _imagePhashDatabaseException.value = e
            _imagePhashDatabase.value = null
        }
    }

    private fun loadKNearestModel(ocrDependencyPaths: OcrDependencyPaths) {
        loadKNearestModel(ocrDependencyPaths.knnModelFile.path)
    }

    private fun loadImagePhashDatabase(ocrDependencyPaths: OcrDependencyPaths) {
        loadImagePhashDatabase(ocrDependencyPaths.phashDatabaseFile.path)
    }

    private fun loadKNearestModel(context: Context) {
        val ocrDependencyPaths = OcrDependencyPaths(context)
        loadKNearestModel(ocrDependencyPaths)
    }

    private fun loadImagePhashDatabase(context: Context) {
        val ocrDependencyPaths = OcrDependencyPaths(context)
        loadImagePhashDatabase(ocrDependencyPaths)
    }

    fun loadAll(context: Context) {
        loadKNearestModel(context)
        loadImagePhashDatabase(context)
    }
}
