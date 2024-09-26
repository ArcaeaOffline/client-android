package xyz.sevive.arcaeaoffline.core.database.externals.arcaea.importers

import kotlinx.serialization.json.Json
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaLanguage
import xyz.sevive.arcaeaoffline.core.database.entities.Pack
import xyz.sevive.arcaeaoffline.core.database.entities.PackLocalized
import xyz.sevive.arcaeaoffline.core.database.externals.arcaea.ArcaeaPacklistRoot


class ArcaeaPacklistImporter(packlistContent: String) {
    companion object {
        /* packs that built in Arcaea but not included in packlist */
        private val BUILT_IN_PACKS = listOf(
            Pack(id = "single", name = "Memory Archive"),
        )
        private val LANGUAGES = listOf(
            ArcaeaLanguage.JA, ArcaeaLanguage.KO, ArcaeaLanguage.ZH_HANS, ArcaeaLanguage.ZH_HANT
        )
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val contentDecoded = json.decodeFromString<ArcaeaPacklistRoot>(packlistContent)

    fun packs(): List<Pack> {
        val items = BUILT_IN_PACKS.toMutableList()
        for (pack in contentDecoded.packs) {
            items.add(
                Pack(
                    id = pack.id,
                    name = pack.nameLocalized.en ?: "Pack",
                    description = pack.descriptionLocalized.en
                )
            )
        }
        return items
    }

    fun packsLocalized(): List<PackLocalized> {
        val items = mutableListOf<PackLocalized>()

        for (pack in contentDecoded.packs) {
            for (lang in LANGUAGES) {
                pack.getPackLocalized(lang)?.let { items.add(it) }
            }
        }

        return items
    }
}
