package xyz.sevive.arcaeaoffline.ui.settings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPackageHelper
import java.io.InputStream

class SettingsViewModel : ViewModel() {
    private val _phashDatabaseBuildProgress = MutableStateFlow(-1)
    val phashDatabaseBuildProgress = _phashDatabaseBuildProgress.asStateFlow()
    private val _phashDatabaseBuildProgressTotal = MutableStateFlow(-1)
    val phashDatabaseBuildProgressTotal = _phashDatabaseBuildProgressTotal.asStateFlow()

    private fun mkOcrDependencyParentDirs(ocrDependencyPaths: OcrDependencyPaths) {
        if (!ocrDependencyPaths.parentDir.exists()) {
            if (!ocrDependencyPaths.parentDir.mkdirs()) {
                Log.w("OCR Dependency", "Cannot create dependencies parent directory")
            }
        }
    }

    fun importKnnModel(
        importFileInputStream: InputStream?, ocrDependencyPaths: OcrDependencyPaths,
    ) {
        mkOcrDependencyParentDirs(ocrDependencyPaths)
        importFileInputStream.use {
            val outputStream = ocrDependencyPaths.knnModelFile.outputStream()
            it?.copyTo(outputStream)
        }
    }

    fun importPhashDatabase(
        importFileInputStream: InputStream?, ocrDependencyPaths: OcrDependencyPaths,
    ) {
        mkOcrDependencyParentDirs(ocrDependencyPaths)
        importFileInputStream.use {
            val outputStream = ocrDependencyPaths.phashDatabaseFile.outputStream()
            it?.copyTo(outputStream)
        }
    }

    suspend fun buildPhashDatabaseFromArcaea(
        context: Context, ocrDependencyPaths: OcrDependencyPaths,
    ) {
        val arcaeaPackageHelper = ArcaeaPackageHelper(context)

        _phashDatabaseBuildProgress.value = 0
        withContext(Dispatchers.IO) {
            arcaeaPackageHelper.buildPhashDatabase(progressCallback = { progress, total ->
                _phashDatabaseBuildProgress.value = progress
                _phashDatabaseBuildProgressTotal.value = total
            })
        }
        _phashDatabaseBuildProgress.value = -1
        _phashDatabaseBuildProgressTotal.value = -1

        importPhashDatabase(
            arcaeaPackageHelper.tempPhashDatabaseFile.inputStream(),
            ocrDependencyPaths
        )
    }
}
