package xyz.sevive.arcaeaoffline.core.ocr

import kotlinx.serialization.Serializable

enum class ImageHashItemHashType(
    val value: Int,
) {
    AVERAGE(0),
    DIFFERENCE(1),
    DCT(2),
}

enum class ImageHashItemType(
    val value: Int,
) {
    JACKET(0),
    PARTNER_ICON(1),
}

@Serializable
data class ImageHashItem(
    val hashType: ImageHashItemHashType,
    val type: ImageHashItemType,
    val label: String,
    val confidence: Double,
)

/**
 * Picks the winning label across per-hash-type lookup results and returns its best entry.
 *
 * A label's score is the weighted average of its entries' confidences, weighting DCT 4,
 * DIFFERENCE 2 and AVERAGE 1. Score ties resolve to the earliest-appearing label.
 *
 * @return The winning label's highest-confidence entry, with [ImageHashItem.confidence]
 *   replaced by the label's score rather than the entry's raw confidence. `null` on an
 *   empty list.
 */
fun List<ImageHashItem>.getMostConfidentItem(): ImageHashItem? {
    if (isEmpty()) return null

    data class Accumulator(
        val weightedConfidenceSum: Double,
        val weightSum: Double,
    )

    val labelAccumulators = linkedMapOf<String, Accumulator>()
    for ((hashType, _, label, confidence) in this) {
        val weight =
            when (hashType) {
                ImageHashItemHashType.AVERAGE -> 1.0
                ImageHashItemHashType.DIFFERENCE -> 2.0
                ImageHashItemHashType.DCT -> 4.0
            }
        val acc = labelAccumulators[label] ?: Accumulator(0.0, 0.0)
        labelAccumulators[label] =
            Accumulator(acc.weightedConfidenceSum + confidence * weight, acc.weightSum + weight)
    }

    val winner =
        labelAccumulators.entries.maxByOrNull { it.value.weightedConfidenceSum / it.value.weightSum }
            ?: return null
    val fused = winner.value.weightedConfidenceSum / winner.value.weightSum

    return this.filter { it.label == winner.key }.maxByOrNull { it.confidence }?.copy(confidence = fused)
}
