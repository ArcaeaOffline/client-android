package xyz.sevive.arcaeaoffline.datastore

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PreferencesMetadata(
    @SerialName("version")
    val version: Int = 1,
)
