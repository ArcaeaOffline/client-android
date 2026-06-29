package xyz.sevive.arcaeaoffline.database.entities

import kotlinx.serialization.Serializable

@Serializable
data class OcrQueueStagingOptions(
    val checkIsImage: Boolean = true,
    val checkIsArcaeaImage: Boolean = true,
)
