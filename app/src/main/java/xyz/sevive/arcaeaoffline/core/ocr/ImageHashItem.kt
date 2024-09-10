package xyz.sevive.arcaeaoffline.core.ocr

import kotlinx.serialization.Serializable

enum class ImageHashItemHashType(val value: Int) {
    AVERAGE(0), DIFFERENCE(1), DCT(2);
}

enum class ImageHashItemType(val value: Int) {
    JACKET(0), PARTNER_ICON(1);
}

@Serializable
data class ImageHashItem(
    val hashType: ImageHashItemHashType,
    val type: ImageHashItemType,
    val label: String,
    val confidence: Double,
)

fun List<ImageHashItem>.getMostConfidentItem(): ImageHashItem? {
    if (isEmpty()) return null

    val labelConfidenceMap = this.map { it.label }.distinct().associateWith { 0.0 }.toMutableMap()

    this.forEach {
        val weight = when (it.hashType) {
            ImageHashItemHashType.AVERAGE -> 0.3
            ImageHashItemHashType.DCT -> 0.2
            ImageHashItemHashType.DIFFERENCE -> 0.2
        }
        val value = labelConfidenceMap[it.label] ?: 0.0
        labelConfidenceMap[it.label] = value + it.confidence * weight
    }

    return this.sortedByDescending { it.confidence }
        .first { it.label == labelConfidenceMap.maxBy { mapItem -> mapItem.value }.key }
}
