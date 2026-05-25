package xyz.sevive.arcaeaoffline.helpers

import android.content.Context
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import java.io.File

object OcrDependencyLoader {
    fun kNearestModel(file: File): KNearest = KNearest.load(file.absolutePath)

    fun kNearestModel(ocrDependencyPaths: OcrDependencyPaths) = kNearestModel(ocrDependencyPaths.knnModelFile)

    fun kNearestModel(context: Context) = kNearestModel(OcrDependencyPaths(context))

    fun imageHashesSQLiteDatabase(file: File): SQLiteConnection = BundledSQLiteDriver().open(file.absolutePath, SQLITE_OPEN_READONLY)

    fun imageHashesSQLiteDatabase(ocrDependencyPaths: OcrDependencyPaths) =
        imageHashesSQLiteDatabase(ocrDependencyPaths.imageHashesDatabaseFile)

    fun imageHashesSQLiteDatabase(context: Context) = imageHashesSQLiteDatabase(OcrDependencyPaths(context))
}
