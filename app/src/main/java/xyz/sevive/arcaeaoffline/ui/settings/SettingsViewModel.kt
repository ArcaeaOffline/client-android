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
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaButtonDefaults
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaButtonState
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

    private fun arcaeaAssetsAvailable(arcaeaPackageHelper: ArcaeaPackageHelper): Boolean {
        if (!arcaeaPackageHelper.isInstalled()) return false
        return arcaeaPackageHelper.apkJacketZipEntries().size + arcaeaPackageHelper.apkPartnerIconZipEntries().size > 0
    }

    fun buildPhashDatabaseFromArcaeaButtonState(context: Context): ArcaeaButtonState {
        val defaultState = ArcaeaButtonDefaults.state(context)
        val arcaeaPackageHelper = ArcaeaPackageHelper(context)
        if (defaultState != ArcaeaButtonState.NORMAL) return defaultState
        return if (arcaeaAssetsAvailable(arcaeaPackageHelper)) {
            defaultState
        } else {
            ArcaeaButtonState.RESOURCE_UNAVAILABLE
        }
    }

    suspend fun buildPhashDatabaseFromArcaea(
        context: Context, ocrDependencyPaths: OcrDependencyPaths,
    ) {
        val arcaeaPackageHelper = ArcaeaPackageHelper(context)
        if (!arcaeaAssetsAvailable(arcaeaPackageHelper)) return

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
