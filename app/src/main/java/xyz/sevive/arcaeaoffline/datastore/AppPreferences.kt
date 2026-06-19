package xyz.sevive.arcaeaoffline.datastore

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.akuleshov7.ktoml.Toml
import kotlinx.coroutines.CancellationException
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

    @Suppress("BlockingMethodInNonBlockingContext")
    override suspend fun writeTo(
        t: AppPreferences,
        output: OutputStream,
    ) = output.write(Toml.encodeToString<AppPreferences>(t).encodeToByteArray())
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
