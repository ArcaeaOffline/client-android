package xyz.sevive.arcaeaoffline.core.database.export

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encodeToString
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import java.util.UUID


/**
 * @see <a href="https://stackoverflow.com/a/65398285/16484891">SO answer, CC BY-SA 4.0</a>
 */
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}


@Serializable
data class ArcaeaOfflineExportPlayResultItem(
    val id: Long,
    @Serializable(UUIDSerializer::class) val uuid: UUID,
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
    val source: String? = null,
    val comment: String? = null,
)

@Serializable
data class ArcaeaOfflineExportScoreRoot(
    val type: String,
    val version: Int,
    val scores: List<ArcaeaOfflineExportPlayResultItem>,
)

class ArcaeaOfflineExportScore(private val playResultRepository: PlayResultRepository) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun toJsonObject(): ArcaeaOfflineExportScoreRoot? {
        val scores = playResultRepository.findAll().firstOrNull() ?: return null

        val exportScores = mutableListOf<ArcaeaOfflineExportPlayResultItem>()
        for (score in scores) {
            exportScores.add(
                ArcaeaOfflineExportPlayResultItem(
                    id = score.id,
                    uuid = score.uuid,
                    songId = score.songId,
                    ratingClass = score.ratingClass.value,
                    score = score.score,
                    pure = score.pure,
                    far = score.far,
                    lost = score.lost,
                    date = score.date?.toEpochMilli(),
                    maxRecall = score.maxRecall,
                    modifier = score.modifier?.value,
                    clearType = score.clearType?.value,
                    source = "https://arcaeaoffline.sevive.xyz/android",
                    comment = score.comment,
                )
            )
        }

        return ArcaeaOfflineExportScoreRoot(
            type = "score", version = 2, scores = exportScores.toList()
        )
    }

    fun toJsonString(obj: ArcaeaOfflineExportScoreRoot): String {
        return json.encodeToString(obj)
    }
}
