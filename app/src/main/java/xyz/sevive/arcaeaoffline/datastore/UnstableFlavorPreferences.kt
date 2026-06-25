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
data class UnstableFlavorPreferences(
    val metadata: PreferencesMetadata = PreferencesMetadata(),
    @SerialName("unstable_alert_read")
    val unstableAlertRead: Boolean = false,
)

object UnstableFlavorPreferencesSerializer : OkioSerializer<UnstableFlavorPreferences> {
    override val defaultValue = UnstableFlavorPreferences()

    override suspend fun readFrom(source: BufferedSource): UnstableFlavorPreferences =
        try {
            Toml.decodeFromString<UnstableFlavorPreferences>(source.readUtf8())
        } catch (e: CancellationException) {
            throw e
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read UnstableFlavorPreferences from TOML file", exception)
        }

    override suspend fun writeTo(
        t: UnstableFlavorPreferences,
        sink: BufferedSink,
    ) {
        sink.writeUtf8(Toml.encodeToString<UnstableFlavorPreferences>(t))
    }
}

class UnstableFlavorPreferencesRepository(
    context: Context,
) {
    private val dataStore = AppDataStoreProvider.unstableFlavorPreferences(context)
    val preferencesFlow = dataStore.data

    suspend fun setAlertRead(value: Boolean) {
        dataStore.updateData { preferences ->
            preferences.copy(unstableAlertRead = value)
        }
    }
}
