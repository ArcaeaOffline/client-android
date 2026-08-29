package xyz.sevive.arcaeaoffline.helpers

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.driver.bundled.SQLITE_OPEN_READONLY
import kotlinx.io.files.Path
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths

object OcrDependencyLoader {
    fun imageHashesSQLiteDatabase(path: Path): SQLiteConnection = BundledSQLiteDriver().open(path.toString(), SQLITE_OPEN_READONLY)

    fun imageHashesSQLiteDatabase(ocrDependencyPaths: OcrDependencyPaths) =
        imageHashesSQLiteDatabase(ocrDependencyPaths.imageHashesDatabaseFile)

    fun imageHashesSQLiteDatabase() = imageHashesSQLiteDatabase(OcrDependencyPaths())
}
