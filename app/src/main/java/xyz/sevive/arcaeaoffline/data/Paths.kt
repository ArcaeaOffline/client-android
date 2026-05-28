package xyz.sevive.arcaeaoffline.data

import android.content.Context
import okio.Path.Companion.toOkioPath

class OcrDependencyPaths(
    context: Context,
) {
    val parentDir = context.filesDir.toOkioPath() / "ocr" / "dependencies"

    val knnModelFile = parentDir / "digits.knn.dat"
    val phashDatabaseFile = parentDir / "image-phash.db"
    val imageHashesDatabaseFile = parentDir / "image-hashes.db"
}

class OcrPaths(
    context: Context,
) {
    val parentDir = context.filesDir.toOkioPath() / "ocr"

    val fromShareImageCacheDir = parentDir / "fromShareImages"
}

class Paths(
    context: Context,
) {
    val ocr = OcrPaths(context)
    val ocrDependency = OcrDependencyPaths(context)
}
