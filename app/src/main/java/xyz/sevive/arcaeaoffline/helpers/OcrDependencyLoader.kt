package xyz.sevive.arcaeaoffline.helpers

import android.content.Context
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import okio.Path
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths

object OcrDependencyLoader {
    fun kNearestModel(path: Path): KNearest = KNearest.load(path.toString())

    fun kNearestModel(ocrDependencyPaths: OcrDependencyPaths) = kNearestModel(ocrDependencyPaths.knnModelFile)

    fun kNearestModel(context: Context) = kNearestModel(OcrDependencyPaths(context))

    fun imageHashesSQLiteDatabase(path: Path): SQLiteConnection = BundledSQLiteDriver().open(path.toString(), SQLITE_OPEN_READONLY)

    fun imageHashesSQLiteDatabase(ocrDependencyPaths: OcrDependencyPaths) =
        imageHashesSQLiteDatabase(ocrDependencyPaths.imageHashesDatabaseFile)

    fun imageHashesSQLiteDatabase(context: Context) = imageHashesSQLiteDatabase(OcrDependencyPaths(context))
}
