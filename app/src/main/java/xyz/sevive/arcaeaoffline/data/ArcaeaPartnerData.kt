package xyz.sevive.arcaeaoffline.data

import android.content.res.AssetManager
import kotlinx.serialization.json.Json
import org.apache.commons.io.IOUtils
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreClearType
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreModifier
import xyz.sevive.arcaeaoffline.constants.arcaea.score.ArcaeaScoreModifierRange
import java.nio.charset.StandardCharsets


class ArcaeaPartnerModifiers(assetManager: AssetManager? = null) {
    private var partnerModifiers: Map<String, ArcaeaScoreModifier> = mapOf()

    init {
        if (assetManager != null) {
            loadFromAssets(assetManager)
        }
    }

    fun loadFromAssets(assetManager: AssetManager) {
        val inputStream = assetManager.open("partnerModifiers.json")
        val content = IOUtils.toString(inputStream, StandardCharsets.UTF_8)
        val contentParsed = parsePartnerModifiersJson(content)
        updateWith(contentParsed)
    }

    fun updateWith(value: Map<String, ArcaeaScoreModifier>) {
        partnerModifiers += value
    }

    operator fun get(partnerId: String): ArcaeaScoreModifier {
        return partnerModifiers[partnerId] ?: ArcaeaScoreModifier.NORMAL
    }

    companion object {
        private val PARTNER_ID_REGEX = """^\d+u?$""".toRegex()

        private val format = Json { ignoreUnknownKeys = true }

        fun parsePartnerModifiersJson(jsonContent: String): Map<String, ArcaeaScoreModifier> {
            return format.decodeFromString<Map<String, Int>>(jsonContent).filter {
                PARTNER_ID_REGEX.find(it.key) != null && ArcaeaScoreModifierRange.contains(it.value)
            }.mapValues {
                ArcaeaScoreModifier.fromInt(it.value)
            }
        }
    }
}

fun clearStatusToClearType(clearStatus: Int, modifier: ArcaeaScoreModifier): ArcaeaScoreClearType {
    return when (clearStatus) {
        0 -> ArcaeaScoreClearType.TRACK_LOST

        1 -> when (modifier) {
            ArcaeaScoreModifier.HARD -> ArcaeaScoreClearType.HARD_CLEAR
            ArcaeaScoreModifier.EASY -> ArcaeaScoreClearType.EASY_CLEAR
            else -> ArcaeaScoreClearType.NORMAL_CLEAR
        }

        2 -> ArcaeaScoreClearType.FULL_RECALL
        3 -> ArcaeaScoreClearType.PURE_MEMORY
        else -> throw IllegalArgumentException()
    }
}
