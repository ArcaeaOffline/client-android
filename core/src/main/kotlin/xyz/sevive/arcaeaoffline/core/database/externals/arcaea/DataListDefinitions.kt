package xyz.sevive.arcaeaoffline.core.database.externals.arcaea

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LocalizedObject(
    val en: String? = null,
    val ja: String? = null,
    val ko: String? = null,
    @SerialName("zh-Hant") val zhHant: String? = null,
    @SerialName("zh-Hans") val zhHans: String? = null,
)

@Serializable
data class LocalizedArrayObject(
    val en: List<String>? = null,
    val ja: List<String>? = null,
    val ko: List<String>? = null,
    @SerialName("zh-Hant") val zhHant: List<String>? = null,
    @SerialName("zh-Hans") val zhHans: List<String>? = null,
)

@Serializable
data class PacklistItem(
    val id: String,
    @SerialName("name_localized") val nameLocalized: LocalizedObject,
    @SerialName("description_localized") val descriptionLocalized: LocalizedObject,
)

@Serializable
data class PacklistRoot(
    val packs: List<PacklistItem>
)

@Serializable
data class SonglistBgDayNightItem(
    val day: String,
    val night: String,
)

@Serializable
data class SonglistDifficultyItem(
    val ratingClass: Int,
    val chartDesigner: String,
    val jacketDesigner: String,
    val rating: Int,
    val ratingPlus: Boolean? = null,
    val jacketOverride: Boolean? = null,
    val audioOverride: Boolean? = null,
    val artist: String? = null,
    @SerialName("title_localized") val titleLocalized: LocalizedObject? = null,
    @SerialName("artist_localized") val artistLocalized: LocalizedObject? = null,
    @SerialName("jacket_night") val jacketNight: String? = null,
    val bg: String? = null,
    @SerialName("bg_inverse") val bgInverse: String? = null,
    val bpm: String? = null,
    @SerialName("bpm_base") val bpmBase: Double? = null,
    val version: String? = null,
    val date: Int? = null,
)

@Serializable
data class SonglistItem(
    val idx: Int,
    val id: String,
    @SerialName("title_localized") val titleLocalized: LocalizedObject,
    val artist: String,
    @SerialName("search_title") val searchTitle: LocalizedArrayObject? = null,
    @SerialName("search_artist") val searchArtist: LocalizedArrayObject? = null,
    val bpm: String,
    @SerialName("bpm_base") val bpmBase: Double,
    val set: String,
    val audioPreview: Int,
    val audioPreviewEnd: Int,
    val side: Int,
    val bg: String,
    @SerialName("bg_inverse") val bgInverse: String? = null,
    @SerialName("bg_daynight") val bgDayNight: SonglistBgDayNightItem? = null,
    @SerialName("source_localized") val sourceLocalized: LocalizedObject? = null,
    @SerialName("source_copyright") val sourceCopyright: String? = null,
    val date: Int,
    val version: String,
    val difficulties: List<SonglistDifficultyItem>
)

@Serializable
data class SonglistRoot(
    val songs: List<SonglistItem>
)
