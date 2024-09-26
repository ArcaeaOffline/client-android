package xyz.sevive.arcaeaoffline.core.database.externals.arcaea

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


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
)

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
)

@Serializable
data class ArcaeaSonglistItem(
    val idx: Int,
    val id: String,
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
)

@Serializable
data class ArcaeaSonglistRoot(
    val songs: List<ArcaeaSonglistItem>
)
