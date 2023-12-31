package xyz.sevive.arcaeaoffline.core.database.export

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import xyz.sevive.arcaeaoffline.core.database.repositories.ScoreRepository

@Serializable
data class ArcaeaOfflineExportScoreItem(
    val id: Int,
    val uuid: Nothing? = null,
    val songId: String,
    val ratingClass: Int,
    val score: Int,
    val pure: Int? = null,
    val far: Int? = null,
    val lost: Int? = null,
    val date: Long? = null,
    val maxRecall: Int? = null,
    val modifier: Int? = null,
    val clearType: Int? = null,
    val comment: String? = null,
)

@Serializable
data class ArcaeaOfflineExportScoreRoot(
    val type: String,
    val version: Int,
    val scores: List<ArcaeaOfflineExportScoreItem>,
)

class ArcaeaOfflineExportScore(private val scoreRepository: ScoreRepository) {
    private val json = Json { ignoreUnknownKeys = true }

    private suspend fun toJsonObject(): ArcaeaOfflineExportScoreRoot? {
        val scores = scoreRepository.findAll().firstOrNull() ?: return null

        val exportScores = mutableListOf<ArcaeaOfflineExportScoreItem>()
        for (score in scores) {
            exportScores.add(
                ArcaeaOfflineExportScoreItem(
                    id = score.id,
                    songId = score.songId,
                    ratingClass = score.ratingClass,
                    score = score.score,
                    pure = score.pure,
                    far = score.far,
                    lost = score.lost,
                    date = score.date,
                    maxRecall = score.maxRecall,
                    modifier = score.modifier,
                    clearType = score.clearType,
                    comment = score.comment,
                )
            )
        }

        return ArcaeaOfflineExportScoreRoot(
            type = "score", version = 2, scores = exportScores.toList()
        )
    }

    suspend fun toJsonString(): String? {
        val result = toJsonObject()
        return if (result != null) {
            json.encodeToString(result)
        } else {
            null
        }
    }
}
