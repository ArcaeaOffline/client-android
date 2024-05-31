package xyz.sevive.arcaeaoffline.ui.settings

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.helpers.ArcaeaPackageHelper
import xyz.sevive.arcaeaoffline.helpers.GlobalOcrDependencyHelper
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaButtonDefaults
import xyz.sevive.arcaeaoffline.ui.components.ArcaeaButtonState
import java.io.InputStream

class SettingsViewModel : ViewModel() {
    data class PhashDatabaseBuildProgress(
        val progress: Int = -1,
        val total: Int = -1,
    )

    private val _phashDatabaseBuildProgress = MutableStateFlow<PhashDatabaseBuildProgress?>(null)
    val phashDatabaseBuildProgress = _phashDatabaseBuildProgress.asStateFlow()

    private fun mkOcrDependencyParentDirs(ocrDependencyPaths: OcrDependencyPaths) {
        if (!ocrDependencyPaths.parentDir.exists()) {
            if (!ocrDependencyPaths.parentDir.mkdirs()) {
                Log.w("OCR Dependency", "Cannot create dependencies parent directory")
            }
        }
    }

    fun importKnnModel(uri: Uri, context: Context) {
        val ocrDependencyPaths = OcrDependencyPaths(context)
        mkOcrDependencyParentDirs(ocrDependencyPaths)

        val inputStream = context.contentResolver.openInputStream(uri)
        inputStream.use {
            val outputStream = ocrDependencyPaths.knnModelFile.outputStream()
            it?.copyTo(outputStream)
        }
        GlobalOcrDependencyHelper.loadKnnModel(context)
    }

    private fun importPhashDatabase(
        importFileInputStream: InputStream?, ocrDependencyPaths: OcrDependencyPaths,
    ) {
        mkOcrDependencyParentDirs(ocrDependencyPaths)
        importFileInputStream.use {
            val outputStream = ocrDependencyPaths.phashDatabaseFile.outputStream()
            it?.copyTo(outputStream)
        }
        GlobalOcrDependencyHelper.loadImagePhashDatabase(ocrDependencyPaths)
    }

    fun importPhashDatabase(uri: Uri, context: Context) {
        importPhashDatabase(
            importFileInputStream = context.contentResolver.openInputStream(uri),
            ocrDependencyPaths = OcrDependencyPaths(context),
        )
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

    fun buildPhashDatabaseFromArcaea(context: Context) {
        val arcaeaPackageHelper = ArcaeaPackageHelper(context)
        if (!arcaeaAssetsAvailable(arcaeaPackageHelper)) return

        val ocrDependencyPaths = OcrDependencyPaths(context)

        viewModelScope.launch {
            _phashDatabaseBuildProgress.value = PhashDatabaseBuildProgress()
            withContext(Dispatchers.IO) {
                arcaeaPackageHelper.buildPhashDatabase(progressCallback = { progress, total ->
                    _phashDatabaseBuildProgress.value = PhashDatabaseBuildProgress(progress, total)
                })
            }

            importPhashDatabase(
                arcaeaPackageHelper.tempPhashDatabaseFile.inputStream(),
                ocrDependencyPaths
            )

            _phashDatabaseBuildProgress.value = null
            arcaeaPackageHelper.tempPhashDatabaseFile.delete()
        }
    }
}
