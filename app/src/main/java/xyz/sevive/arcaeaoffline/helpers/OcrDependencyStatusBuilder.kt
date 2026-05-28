package xyz.sevive.arcaeaoffline.helpers

import android.content.Context
import okio.FileSystem
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrOnnxHelper
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import kotlin.use

object OcrDependencyStatusBuilder {
    fun kNearest(context: Context): KNearestModelStatusDetail {
        try {
            val paths = OcrDependencyPaths(context)
            if (!FileSystem.SYSTEM.exists(paths.knnModelFile)) return KNearestModelStatusDetail(absence = true)

            val model = OcrDependencyLoader.kNearestModel(context)
            return KNearestModelStatusDetail(varCount = model.varCount, isTrained = model.isTrained)
        } catch (e: Exception) {
            return KNearestModelStatusDetail(exception = e)
        }
    }

    fun imageHashesDatabase(context: Context): ImageHashesDatabaseStatusDetail {
        try {
            val paths = OcrDependencyPaths(context)
            if (!FileSystem.SYSTEM.exists(paths.imageHashesDatabaseFile)) {
                return ImageHashesDatabaseStatusDetail(
                    absence = true,
                )
            }

            OcrDependencyLoader.imageHashesSQLiteDatabase(context).use { sqliteDb ->
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
        DeviceOcrOnnxHelper.createOrtSession(context).use {
            try {
                CrnnModelStatusDetail(
                    modelMetadata = it.metadata,
                    inputNames = it.inputNames.toSet(), // make a copy, same for below
                    outputNames = it.outputNames.toSet(),
                )
            } catch (e: Exception) {
                CrnnModelStatusDetail(exception = e)
            }
        }
}
