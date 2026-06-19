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
data class AppPreferences(
    val metadata: PreferencesMetadata = PreferencesMetadata(),
    @SerialName("auto_send_crash_reports")
    val autoSendCrashReports: Boolean = false,
)

object AppPreferencesSerializer : Serializer<AppPreferences> {
    override val defaultValue: AppPreferences = AppPreferences()

    override suspend fun readFrom(input: InputStream): AppPreferences =
        try {
            val tomlString = input.readBytes().decodeToString()
            Toml.decodeFromString<AppPreferences>(tomlString)
        } catch (e: CancellationException) {
            throw e
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read AppPreferences from TOML file", exception)
        }

    override suspend fun writeTo(
        t: AppPreferences,
        output: OutputStream,
    ) {
        val tomlString = Toml.encodeToString<AppPreferences>(t)
        withContext(Dispatchers.IO) {
            output.write(tomlString.encodeToByteArray())
        }
    }
}

class AppPreferencesRepository(
    context: Context,
) {
    private val dataStore = AppDataStoreProvider.appPreferences(context)
    val preferencesFlow = dataStore.data

    suspend fun setAutoSendCrashReports(value: Boolean) {
        dataStore.updateData { preferences ->
            preferences.copy(autoSendCrashReports = value)
        }
    }
}
