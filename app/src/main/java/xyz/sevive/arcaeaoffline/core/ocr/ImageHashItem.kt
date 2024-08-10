package xyz.sevive.arcaeaoffline.core.ocr

enum class ImageHashItemHashType(val value: Int) {
    AVERAGE(0), DIFFERENCE(1), DCT(2);
}

enum class ImageHashItemType(val value: Int) {
    JACKET(0), PARTNER_ICON(1);
}

data class ImageHashItem(
    val hashType: ImageHashItemHashType,
    val type: ImageHashItemType,
    val label: String,
    val confidence: Double,
)
