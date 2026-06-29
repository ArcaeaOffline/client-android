package xyz.sevive.arcaeaoffline.database.entities

import kotlinx.serialization.Serializable

@Serializable
data class OcrQueueStagingOptions(
    val checkIsImage: Boolean,
    val checkIsArcaeaImage: Boolean,
) {
    companion object {
        val DEFAULTS =
            OcrQueueStagingOptions(
                checkIsImage = true,
                checkIsArcaeaImage = true,
            )
    }
}
