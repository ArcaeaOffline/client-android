package xyz.sevive.arcaeaoffline.core.ocr

import android.content.Context
import io.requery.android.database.sqlite.SQLiteDatabase
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import java.io.File

object OcrDependencyLoader {
    fun kNearestModel(file: File): KNearest {
        return KNearest.load(file.absolutePath)
    }

    fun kNearestModel(ocrDependencyPaths: OcrDependencyPaths) =
        kNearestModel(ocrDependencyPaths.knnModelFile)

    fun kNearestModel(context: Context) = kNearestModel(OcrDependencyPaths(context))

    fun imageHashesSQLiteDatabase(file: File): SQLiteDatabase {
        return SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
    }

    fun imageHashesSQLiteDatabase(ocrDependencyPaths: OcrDependencyPaths) =
        imageHashesSQLiteDatabase(ocrDependencyPaths.imageHashesDatabaseFile)

    fun imageHashesSQLiteDatabase(context: Context) =
        imageHashesSQLiteDatabase(OcrDependencyPaths(context))
}
