package xyz.sevive.arcaeaoffline.core

import android.content.res.AssetManager
import kotlinx.serialization.json.Json
import org.apache.commons.io.IOUtils
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifierRange
import java.nio.charset.StandardCharsets


class ArcaeaPartnerModifiers(assetManager: AssetManager? = null) {
    private var partnerModifiers: Map<String, ArcaeaPlayResultModifier> = mapOf()

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

    fun updateWith(value: Map<String, ArcaeaPlayResultModifier>) {
        partnerModifiers += value
    }

    operator fun get(partnerId: String): ArcaeaPlayResultModifier {
        return partnerModifiers[partnerId] ?: ArcaeaPlayResultModifier.NORMAL
    }

    companion object {
        private val PARTNER_ID_REGEX = """^\d+u?$""".toRegex()

        private val format = Json { ignoreUnknownKeys = true }

        fun parsePartnerModifiersJson(jsonContent: String): Map<String, ArcaeaPlayResultModifier> {
            return format.decodeFromString<Map<String, Int>>(jsonContent).filter {
                PARTNER_ID_REGEX.find(it.key) != null && ArcaeaPlayResultModifierRange.contains(it.value)
            }.mapValues {
                ArcaeaPlayResultModifier.fromInt(it.value)
            }
        }
    }
}

fun clearStatusToClearType(
    clearStatus: Int,
    modifier: ArcaeaPlayResultModifier
): ArcaeaPlayResultClearType {
    return when (clearStatus) {
        0 -> ArcaeaPlayResultClearType.TRACK_LOST

        1 -> when (modifier) {
            ArcaeaPlayResultModifier.HARD -> ArcaeaPlayResultClearType.HARD_CLEAR
            ArcaeaPlayResultModifier.EASY -> ArcaeaPlayResultClearType.EASY_CLEAR
            else -> ArcaeaPlayResultClearType.NORMAL_CLEAR
        }

        2 -> ArcaeaPlayResultClearType.FULL_RECALL
        3 -> ArcaeaPlayResultClearType.PURE_MEMORY
        else -> throw IllegalArgumentException()
    }
}
