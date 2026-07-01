package xyz.sevive.arcaeaoffline.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifierRange
import xyz.sevive.arcaeaoffline.resources.Res

object ArcaeaPartnerModifiers {
    private val PARTNER_ID_REGEX = """^\d+u?$""".toRegex()

    private val format = Json { ignoreUnknownKeys = true }

    private var partnerModifiers: Map<String, ArcaeaPlayResultModifier> = mapOf()

    init {
        CoroutineScope(Dispatchers.IO).launch { loadFromRes() }
    }

    suspend fun loadFromRes() {
        val bundledContent = Res.readBytes("files/partnerModifiers.json").decodeToString()
        updateWith(parsePartnerModifiersJson(bundledContent))
    }

    fun updateWith(value: Map<String, ArcaeaPlayResultModifier>) {
        partnerModifiers += value
    }

    operator fun get(partnerId: String): ArcaeaPlayResultModifier = partnerModifiers[partnerId] ?: ArcaeaPlayResultModifier.NORMAL

    fun parsePartnerModifiersJson(jsonContent: String): Map<String, ArcaeaPlayResultModifier> =
        format
            .decodeFromString<Map<String, Int>>(jsonContent)
            .filter {
                PARTNER_ID_REGEX.find(it.key) != null && ArcaeaPlayResultModifierRange.contains(it.value)
            }.mapValues {
                ArcaeaPlayResultModifier.fromInt(it.value)
            }
}

fun clearStatusToClearType(
    clearStatus: Int,
    modifier: ArcaeaPlayResultModifier,
): ArcaeaPlayResultClearType =
    when (clearStatus) {
        0 -> {
            ArcaeaPlayResultClearType.TRACK_LOST
        }

        1 -> {
            when (modifier) {
                ArcaeaPlayResultModifier.HARD -> ArcaeaPlayResultClearType.HARD_CLEAR
                ArcaeaPlayResultModifier.EASY -> ArcaeaPlayResultClearType.EASY_CLEAR
                else -> ArcaeaPlayResultClearType.NORMAL_CLEAR
            }
        }

        2 -> {
            ArcaeaPlayResultClearType.FULL_RECALL
        }

        3 -> {
            ArcaeaPlayResultClearType.PURE_MEMORY
        }

        else -> {
            throw IllegalArgumentException()
        }
    }
