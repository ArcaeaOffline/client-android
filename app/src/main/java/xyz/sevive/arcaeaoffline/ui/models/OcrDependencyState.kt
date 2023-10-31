package xyz.sevive.arcaeaoffline.ui.models

import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.ocr.ImagePhashDatabase

data class KnnModelState (
    val model: KNearest? = null,
    val error: Exception? = null
)

data class PhashDatabaseState(
    val db: ImagePhashDatabase? = null,
    val error: Exception? = null,
)
