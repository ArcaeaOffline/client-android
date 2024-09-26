package xyz.sevive.arcaeaoffline.core.database.externals.exporters

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.database.externals.ArcaeaOfflineDEFv2PlayResultItem
import xyz.sevive.arcaeaoffline.core.database.externals.ArcaeaOfflineDEFv2PlayResultRoot


object ArcaeaOfflineDEFv2Exporter {
    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
    }

    fun playResultsRoot(playResults: List<PlayResult>): ArcaeaOfflineDEFv2PlayResultRoot {
        val items = mutableListOf<ArcaeaOfflineDEFv2PlayResultItem>()
        playResults.forEach {
            items.add(
                ArcaeaOfflineDEFv2PlayResultItem(
                    id = it.id,
                    uuid = it.uuid,
                    songId = it.songId,
                    ratingClass = it.ratingClass.value,
                    score = it.score,
                    pure = it.pure,
                    far = it.far,
                    lost = it.lost,
                    date = it.date?.toEpochMilli(),
                    maxRecall = it.maxRecall,
                    modifier = it.modifier?.value,
                    clearType = it.clearType?.value,
                    source = "https://arcaeaoffline.sevive.xyz/android",
                    comment = it.comment,
                )
            )
        }
        return ArcaeaOfflineDEFv2PlayResultRoot(playResults = items)
    }

    fun playResults(root: ArcaeaOfflineDEFv2PlayResultRoot): String {
        return json.encodeToString(root)
    }
}
