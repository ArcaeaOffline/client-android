package xyz.sevive.arcaeaoffline.datastore

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.akuleshov7.ktoml.Toml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class UnstableFlavorPreferences(
    @SerialName("unstable_alert_read") val unstableAlertRead: Boolean = false,
)

object UnstableFlavorPreferencesSerializer : Serializer<UnstableFlavorPreferences> {
    override val defaultValue = UnstableFlavorPreferences()

    override suspend fun readFrom(input: InputStream): UnstableFlavorPreferences =
        try {
            val tomlString = input.readBytes().decodeToString()
            Toml.decodeFromString<UnstableFlavorPreferences>(tomlString)
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read UnstableFlavorPreferences from TOML file", exception)
        }

    override suspend fun writeTo(
        t: UnstableFlavorPreferences,
        output: OutputStream,
    ) {
        val tomlString = Toml.encodeToString<UnstableFlavorPreferences>(t)
        withContext(Dispatchers.IO) {
            output.write(tomlString.encodeToByteArray())
        }
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
