package xyz.sevive.arcaeaoffline.helpers

import android.content.Context
import kotlinx.io.files.SystemFileSystem
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrOnnxHelper
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import kotlin.time.Instant
import kotlin.use

object OcrDependencyStatusBuilder {
    fun imageHashesDatabase(): ImageHashesDatabaseStatusDetail {
        try {
            val paths = OcrDependencyPaths()
            if (!SystemFileSystem.exists(paths.imageHashesDatabaseFile)) {
                return ImageHashesDatabaseStatusDetail(
                    absence = true,
                )
            }

            OcrDependencyLoader.imageHashesSQLiteDatabase().use { sqliteDb ->
                val db = ImageHashesDatabase(sqliteDb)

                return ImageHashesDatabaseStatusDetail(
                    jacketCount = db.jacketHashesCount,
                    partnerIconCount = db.partnerIconHashesCount,
                    builtTime = db.builtTime,
                )
            }
        } catch (e: Exception) {
            return ImageHashesDatabaseStatusDetail(exception = e)
        }
    }

    fun crnnModel(context: Context): CrnnModelStatusDetail =
        try {
            val info = DeviceOcrOnnxHelper.loadModelInfoFile(context)
            with(info) {
                CrnnModelStatusDetail(
                    modelVersion = patch?.version?.takeIf { it.isNotEmpty() },
                    producerName = patch?.producerName,
                    producerVersion = patch?.producerVersion,
                    domain = patch?.domain,
                    graphName = patch?.graphName,
                    inputNames = patch?.inputNames?.toSet(),
                    outputNames = patch?.outputNames?.toSet(),
                    builtTimestamp = training.builtTimestamp.takeIf { it != 0L }?.let { Instant.fromEpochSeconds(it) },
                    patchedTimestamp = patch?.patchedTimestamp?.takeIf { it != 0L }?.let { Instant.fromEpochSeconds(it) },
                )
            }
        } catch (e: Exception) {
            CrnnModelStatusDetail(exception = e)
        }
}
