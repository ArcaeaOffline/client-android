package xyz.sevive.arcaeaoffline.datastore

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.okio.OkioSerializer
import com.akuleshov7.ktoml.Toml
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import okio.BufferedSink
import okio.BufferedSource

@Serializable
data class EmergencyModePreferences(
    val metadata: PreferencesMetadata = PreferencesMetadata(),
    @SerialName("last_output_directory")
    val lastOutputDirectory: String? = null,
)

object EmergencyModePreferencesSerializer : OkioSerializer<EmergencyModePreferences> {
    override val defaultValue = EmergencyModePreferences()

    override suspend fun readFrom(source: BufferedSource): EmergencyModePreferences =
        try {
            Toml.decodeFromString<EmergencyModePreferences>(source.readUtf8())
        } catch (e: CancellationException) {
            throw e
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read EmergencyModePreferences from TOML file", exception)
        }

    override suspend fun writeTo(
        t: EmergencyModePreferences,
        sink: BufferedSink,
    ) {
        sink.writeUtf8(Toml.encodeToString<EmergencyModePreferences>(t))
    }
}

class EmergencyModePreferencesRepository(
    context: Context,
) {
    private val dataStore = AppDataStoreProvider.emergencyModePreferences(context)
    val preferencesFlow = dataStore.data

    suspend fun updateLastOutputDirectory(path: String?) {
        dataStore.updateData { preferences ->
            preferences.copy(lastOutputDirectory = path)
        }
    }
}
