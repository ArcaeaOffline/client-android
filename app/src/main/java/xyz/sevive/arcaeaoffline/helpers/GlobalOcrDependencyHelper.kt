package xyz.sevive.arcaeaoffline.helpers

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.core.ocr.ImagePhashDatabase
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths


object GlobalOcrDependencyHelper {
    private const val LOG_TAG = "GlobalOcrDepHelper"

    data class KnnModelState(
        val model: KNearest? = null,
        val exception: Exception? = null
    )

    data class PhashDatabaseState(
        val database: ImagePhashDatabase? = null,
        val exception: Exception? = null
    )

    private val _knnModelState = MutableStateFlow(KnnModelState())
    val knnModelState = _knnModelState.asStateFlow()

    private val _phashDatabaseState = MutableStateFlow(PhashDatabaseState())
    val phashDatabaseState = _phashDatabaseState.asStateFlow()

    private fun loadKnnModel(filePath: String) {
        try {
            _knnModelState.value = KnnModelState(model = KNearest.load(filePath))
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error loading KNearest model", e)
            _knnModelState.value = KnnModelState(exception = e)
        }
    }

    private fun loadImagePhashDatabase(filePath: String) {
        try {
            _phashDatabaseState.value = PhashDatabaseState(database = ImagePhashDatabase(filePath))
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error loading image phash database", e)
            _phashDatabaseState.value = PhashDatabaseState(exception = e)
        }
    }

    private fun loadKnnModel(ocrDependencyPaths: OcrDependencyPaths) {
        loadKnnModel(ocrDependencyPaths.knnModelFile.path)
    }

    fun loadImagePhashDatabase(ocrDependencyPaths: OcrDependencyPaths) {
        loadImagePhashDatabase(ocrDependencyPaths.phashDatabaseFile.path)
    }

    fun loadKnnModel(context: Context) {
        val ocrDependencyPaths = OcrDependencyPaths(context)
        loadKnnModel(ocrDependencyPaths)
    }

    fun loadImagePhashDatabase(context: Context) {
        val ocrDependencyPaths = OcrDependencyPaths(context)
        loadImagePhashDatabase(ocrDependencyPaths)
    }

    fun loadAll(context: Context) {
        loadKnnModel(context)
        loadImagePhashDatabase(context)
    }
}
