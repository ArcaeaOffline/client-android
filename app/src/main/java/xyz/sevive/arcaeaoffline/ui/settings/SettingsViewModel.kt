package xyz.sevive.arcaeaoffline.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import java.io.InputStream

class SettingsViewModel : ViewModel() {
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
}
