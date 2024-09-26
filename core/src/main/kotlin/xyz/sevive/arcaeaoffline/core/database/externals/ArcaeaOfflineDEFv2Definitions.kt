package xyz.sevive.arcaeaoffline.core.database.externals

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
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
data class ArcaeaOfflineDEFv2PlayResultItem(
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

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class ArcaeaOfflineDEFv2PlayResultRoot(
    @EncodeDefault val type: String = "score",
    @EncodeDefault val version: Int = 2,
    @SerialName("scores") val playResults: List<ArcaeaOfflineDEFv2PlayResultItem>,
)
