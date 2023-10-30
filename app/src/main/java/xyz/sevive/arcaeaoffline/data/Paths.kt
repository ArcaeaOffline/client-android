package xyz.sevive.arcaeaoffline.data

import android.content.Context
import java.io.File

class OcrDependencyPaths(context: Context) {
    val parentDir = File(context.filesDir, "ocr" + File.separator + "dependencies")

    val knnModelFile = File(parentDir, "digits.knn.dat")
    val phashDatabaseFile = File(parentDir, "image-phash.db")
}

class OcrPaths(context: Context) {
    val parentDir = File(context.filesDir, "ocr")

    val fromShareImageCacheDir = File(parentDir, "fromShareImages")
}

class Paths(context: Context) {
    val ocr = OcrPaths(context)
    val ocrDependency = OcrDependencyPaths(context)
}