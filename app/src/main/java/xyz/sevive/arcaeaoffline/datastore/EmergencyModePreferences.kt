package xyz.sevive.arcaeaoffline.datastore

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.akuleshov7.ktoml.Toml
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class EmergencyModePreferences(
    val metadata: PreferencesMetadata = PreferencesMetadata(),
    @SerialName("last_output_directory")
    val lastOutputDirectory: String? = null,
)

object EmergencyModePreferencesSerializer : Serializer<EmergencyModePreferences> {
    override val defaultValue = EmergencyModePreferences()

    override suspend fun readFrom(input: InputStream): EmergencyModePreferences =
        try {
            val tomlString = input.readBytes().decodeToString()
            Toml.decodeFromString<EmergencyModePreferences>(tomlString)
        } catch (e: CancellationException) {
            throw e
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read EmergencyModePreferences from TOML file", exception)
        }

    override suspend fun writeTo(
        t: EmergencyModePreferences,
        output: OutputStream,
    ) {
        val tomlString = Toml.encodeToString<EmergencyModePreferences>(t)
        withContext(Dispatchers.IO) {
            output.write(tomlString.encodeToByteArray())
        }
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
