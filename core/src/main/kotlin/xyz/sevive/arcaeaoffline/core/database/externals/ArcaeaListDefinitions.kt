package xyz.sevive.arcaeaoffline.core.database.externals

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaLanguage
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.DifficultyLocalized
import xyz.sevive.arcaeaoffline.core.database.entities.PackLocalized
import xyz.sevive.arcaeaoffline.core.database.entities.SongLocalized


@Serializable
data class ArcaeaListLocalizedObject(
    val en: String? = null,
    val ja: String? = null,
    val ko: String? = null,
    @SerialName("zh-Hant") val zhHant: String? = null,
    @SerialName("zh-Hans") val zhHans: String? = null,
)

@Serializable
data class ArcaeaListLocalizedArrayObject(
    val en: List<String>? = null,
    val ja: List<String>? = null,
    val ko: List<String>? = null,
    @SerialName("zh-Hant") val zhHant: List<String>? = null,
    @SerialName("zh-Hans") val zhHans: List<String>? = null,
)

@Serializable
data class ArcaeaPacklistItem(
    val id: String,
    @SerialName("name_localized") val nameLocalized: ArcaeaListLocalizedObject,
    @SerialName("description_localized") val descriptionLocalized: ArcaeaListLocalizedObject,
) {
    fun getPackLocalized(lang: ArcaeaLanguage): PackLocalized? {
        val name = when (lang) {
            ArcaeaLanguage.JA -> nameLocalized.ja
            ArcaeaLanguage.KO -> nameLocalized.ko
            ArcaeaLanguage.ZH_HANS -> nameLocalized.zhHans
            ArcaeaLanguage.ZH_HANT -> nameLocalized.zhHant
            else -> null
        }

        val description = when (lang) {
            ArcaeaLanguage.JA -> descriptionLocalized.ja
            ArcaeaLanguage.KO -> descriptionLocalized.ko
            ArcaeaLanguage.ZH_HANS -> descriptionLocalized.zhHans
            ArcaeaLanguage.ZH_HANT -> descriptionLocalized.zhHant
            else -> null
        }

        return if (name != null || description != null) PackLocalized(
            id = id, lang = lang, name = name, description = description
        ) else null
    }
}

@Serializable
data class ArcaeaPacklistRoot(
    val packs: List<ArcaeaPacklistItem>
)

@Serializable
data class ArcaeaSonglistBgDayNightItem(
    val day: String,
    val night: String,
)

@Serializable
data class ArcaeaSonglistDifficultyItem(
    val ratingClass: Int,
    val chartDesigner: String,
    val jacketDesigner: String,
    val rating: Int,
    val ratingPlus: Boolean? = null,
    val jacketOverride: Boolean? = null,
    val audioOverride: Boolean? = null,
    val artist: String? = null,
    @SerialName("title_localized") val titleLocalized: ArcaeaListLocalizedObject? = null,
    @SerialName("artist_localized") val artistLocalized: ArcaeaListLocalizedObject? = null,
    @SerialName("jacket_night") val jacketNight: String? = null,
    val bg: String? = null,
    @SerialName("bg_inverse") val bgInverse: String? = null,
    val bpm: String? = null,
    @SerialName("bpm_base") val bpmBase: Double? = null,
    val version: String? = null,
    val date: Int? = null,
) {
    fun getDifficultyLocalized(songId: String, lang: ArcaeaLanguage): DifficultyLocalized? {
        val title = when (lang) {
            ArcaeaLanguage.JA -> titleLocalized?.ja
            ArcaeaLanguage.KO -> titleLocalized?.ko
            ArcaeaLanguage.ZH_HANS -> titleLocalized?.zhHans
            ArcaeaLanguage.ZH_HANT -> titleLocalized?.zhHant
            else -> null
        }

        val artist = when (lang) {
            ArcaeaLanguage.JA -> artistLocalized?.ja
            ArcaeaLanguage.KO -> artistLocalized?.ko
            ArcaeaLanguage.ZH_HANS -> artistLocalized?.zhHans
            ArcaeaLanguage.ZH_HANT -> artistLocalized?.zhHant
            else -> null
        }

        return if (title != null || artist != null) DifficultyLocalized(
            songId = songId, ratingClass = ArcaeaRatingClass.fromInt(ratingClass),
            lang = lang, title = title, artist = artist,
        ) else null
    }
}

@Serializable(with = ArcaeaSonglistItemBaseSerializer::class)
sealed class ArcaeaSonglistItemBase {
    abstract val idx: Int
    abstract val id: String
}

@Serializable
data class ArcaeaSonglistItem(
    override val idx: Int,
    override val id: String,
    @SerialName("title_localized") val titleLocalized: ArcaeaListLocalizedObject,
    val artist: String,
    @SerialName("search_title") val searchTitle: ArcaeaListLocalizedArrayObject? = null,
    @SerialName("search_artist") val searchArtist: ArcaeaListLocalizedArrayObject? = null,
    val bpm: String,
    @SerialName("bpm_base") val bpmBase: Double,
    val set: String,
    val audioPreview: Int,
    val audioPreviewEnd: Int,
    val side: Int,
    val bg: String,
    @SerialName("bg_inverse") val bgInverse: String? = null,
    @SerialName("bg_daynight") val bgDayNight: ArcaeaSonglistBgDayNightItem? = null,
    @SerialName("source_localized") val sourceLocalized: ArcaeaListLocalizedObject? = null,
    @SerialName("source_copyright") val sourceCopyright: String? = null,
    val date: Int,
    val version: String,
    val difficulties: List<ArcaeaSonglistDifficultyItem>
) : ArcaeaSonglistItemBase() {
    fun getSongLocalized(lang: ArcaeaLanguage): SongLocalized? {
        val title = when (lang) {
            ArcaeaLanguage.JA -> titleLocalized.ja
            ArcaeaLanguage.KO -> titleLocalized.ko
            ArcaeaLanguage.ZH_HANS -> titleLocalized.zhHans
            ArcaeaLanguage.ZH_HANT -> titleLocalized.zhHant
            else -> null
        }

        val source = when (lang) {
            ArcaeaLanguage.JA -> sourceLocalized?.ja
            ArcaeaLanguage.KO -> sourceLocalized?.ko
            ArcaeaLanguage.ZH_HANS -> sourceLocalized?.zhHans
            ArcaeaLanguage.ZH_HANT -> sourceLocalized?.zhHant
            else -> null
        }

        return if (title != null || source != null) SongLocalized(
            id = id, lang = lang, title = title, source = source
        ) else null
    }
}

@Serializable
data class ArcaeaSonglistItemDeleted(
    override val idx: Int,
    override val id: String,
    val deleted: Boolean,
) : ArcaeaSonglistItemBase()

object ArcaeaSonglistItemBaseSerializer :
    JsonContentPolymorphicSerializer<ArcaeaSonglistItemBase>(ArcaeaSonglistItemBase::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ArcaeaSonglistItemBase> {
        return when (element.jsonObject["deleted"]?.jsonPrimitive?.booleanOrNull) {
            true -> ArcaeaSonglistItemDeleted.serializer()
            else -> ArcaeaSonglistItem.serializer()
        }
    }
}

@Serializable
data class ArcaeaSonglistRoot(
    val songs: List<ArcaeaSonglistItemBase>
)
