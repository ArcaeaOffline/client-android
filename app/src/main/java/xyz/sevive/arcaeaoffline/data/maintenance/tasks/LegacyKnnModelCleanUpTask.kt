package xyz.sevive.arcaeaoffline.data.maintenance.tasks

import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.utils.div
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import xyz.sevive.arcaeaoffline.data.maintenance.AppDataMaintenanceTask

class LegacyKnnModelCleanUpTask : AppDataMaintenanceTask {
    override val id = "legacy_knn_model_cleanup"
    override val version = 2

    private val logger = Logger.withTag("LegacyKnnModelCleanUpTask")

    // Path is kept independent of OcrDependencyPaths.knnModelFile, which was removed
    // together with the KNearest OCR pipeline. This task cleans up the leftover file
    // on devices that installed the app before the removal.
    private val legacyFile: Path =
        Path(FileKit.filesDir.absolutePath()) / "ocr" / "dependencies" / "digits.knn.dat"

    override suspend fun execute() =
        withContext(Dispatchers.IO) {
            if (!SystemFileSystem.exists(legacyFile)) return@withContext
            SystemFileSystem.delete(legacyFile)
            logger.i { "Deleted legacy KNN model file: $legacyFile" }
        }
}
