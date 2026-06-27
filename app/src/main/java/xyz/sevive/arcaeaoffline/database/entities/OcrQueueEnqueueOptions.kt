package xyz.sevive.arcaeaoffline.database.entities

import kotlinx.serialization.Serializable

@Serializable
data class OcrQueueEnqueueOptions(
    val checkIsImage: Boolean = true,
    val checkIsArcaeaImage: Boolean = true,
)
