package xyz.sevive.arcaeaoffline.data

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.utils.div
import kotlinx.io.files.Path

class OcrDependencyPaths {
    val parentDir = Path(FileKit.filesDir.absolutePath()) / "ocr" / "dependencies"

    val knnModelFile = parentDir / "digits.knn.dat"
    val phashDatabaseFile = parentDir / "image-phash.db"
    val imageHashesDatabaseFile = parentDir / "image-hashes.db"
}

class OcrPaths {
    val parentDir = Path(FileKit.filesDir.absolutePath()) / "ocr"

    val fromShareImageCacheDir = parentDir / "fromShareImages"
}

class Paths {
    val ocr = OcrPaths()
    val ocrDependency = OcrDependencyPaths()
}
