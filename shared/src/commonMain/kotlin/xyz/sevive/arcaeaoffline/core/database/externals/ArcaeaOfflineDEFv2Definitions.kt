package xyz.sevive.arcaeaoffline.core.database.externals

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.uuid.Uuid

@Serializable
data class ArcaeaOfflineDEFv2PlayResultItem(
    val id: Long,
    val uuid: Uuid,
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
