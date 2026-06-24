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
data class AppPreferences(
    val metadata: PreferencesMetadata = PreferencesMetadata(),
    @SerialName("auto_send_crash_reports")
    val autoSendCrashReports: Boolean = false,
)

object AppPreferencesSerializer : OkioSerializer<AppPreferences> {
    override val defaultValue: AppPreferences = AppPreferences()

    override suspend fun readFrom(source: BufferedSource): AppPreferences =
        try {
            Toml.decodeFromString<AppPreferences>(source.readUtf8())
        } catch (e: CancellationException) {
            throw e
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read AppPreferences from TOML file", exception)
        }

    override suspend fun writeTo(
        t: AppPreferences,
        sink: BufferedSink,
    ) {
        sink.writeUtf8(Toml.encodeToString<AppPreferences>(t))
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
