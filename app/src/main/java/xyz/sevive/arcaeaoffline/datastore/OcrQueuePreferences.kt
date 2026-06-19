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
data class OcrQueuePreferences(
    val metadata: PreferencesMetadata = PreferencesMetadata(),
    @SerialName("check_is_image")
    val checkIsImage: Boolean = true,
    @SerialName("check_is_arcaea_image")
    val checkIsArcaeaImage: Boolean = true,
    @SerialName("parallel_count")
    val parallelCount: Int = 4,
)

object OcrQueuePreferencesSerializer : Serializer<OcrQueuePreferences> {
    override val defaultValue: OcrQueuePreferences = OcrQueuePreferences()

    override suspend fun readFrom(input: InputStream): OcrQueuePreferences =
        try {
            val tomlString = input.readBytes().decodeToString()
            Toml.decodeFromString<OcrQueuePreferences>(tomlString)
        } catch (e: CancellationException) {
            throw e
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read OcrQueuePreferences from TOML file", exception)
        }

    override suspend fun writeTo(
        t: OcrQueuePreferences,
        output: OutputStream,
    ) {
        val tomlString = Toml.encodeToString<OcrQueuePreferences>(t)
        withContext(Dispatchers.IO) {
            output.write(tomlString.encodeToByteArray())
        }
    }
}

class OcrQueuePreferencesRepository(
    context: Context,
) {
    private val dataStore = AppDataStoreProvider.ocrQueuePreferences(context)
    val preferencesFlow = dataStore.data

    suspend fun setCheckIsImage(value: Boolean) {
        dataStore.updateData { preferences ->
            preferences.copy(checkIsImage = value)
        }
    }

    suspend fun setCheckIsArcaeaImage(value: Boolean) {
        dataStore.updateData { preferences ->
            preferences.copy(checkIsArcaeaImage = value)
        }
    }

    suspend fun setParallelCount(value: Int) {
        dataStore.updateData { preferences ->
            preferences.copy(parallelCount = value)
        }
    }
}
