package xyz.sevive.arcaeaoffline.ui.models

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.core.ocr.ImagePhashDatabase
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import java.io.File
import java.io.FileNotFoundException

class OcrDependencyViewModel : ViewModel() {
    private val _knnModelState = MutableStateFlow(KnnModelState())
    val knnModelState: StateFlow<KnnModelState> = _knnModelState.asStateFlow()

    private val _phashDatabaseState = MutableStateFlow(PhashDatabaseState())
    val phashDatabaseState: StateFlow<PhashDatabaseState> = _phashDatabaseState.asStateFlow()

    private var ocrDependencyPaths: OcrDependencyPaths? = null

    fun setOcrDependencyPaths(paths: OcrDependencyPaths) {
        ocrDependencyPaths = paths
    }

    fun reload(ocrDependencyPaths: OcrDependencyPaths? = null) {
        val paths = ocrDependencyPaths ?: this.ocrDependencyPaths ?: throw IllegalArgumentException(
            "Cannot load from a null `ocrDependencyPaths`"
        )

        loadKnnModel(paths.knnModelFile)
        loadPhashDatabase(paths.phashDatabaseFile)
    }

    fun loadKnnModel(file: File) {
        val newKnnModelState = try {
            if (!file.exists()) throw FileNotFoundException("${file.path} does not exist.")
            KnnModelState(model = KNearest.load(file.path), error = null)
        } catch (e: Exception) {
            Log.e("OcrDependencyViewModel", "Error loading knn model", e)
            KnnModelState(model = null, error = e)
        }
        _knnModelState.value = newKnnModelState
    }

    fun loadPhashDatabase(file: File) {
        val newPhashDatabaseState = try {
            PhashDatabaseState(db = ImagePhashDatabase(file.path), error = null)
        } catch (e: Exception) {
            Log.e("OcrDependencyViewModel", "Error loading phash database", e)
            PhashDatabaseState(db = null, error = e)
        }
        _phashDatabaseState.value = newPhashDatabaseState
    }
}
