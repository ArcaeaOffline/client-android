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
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueStagingOptions

@Serializable
data class OcrQueuePreferences(
    val metadata: PreferencesMetadata = PreferencesMetadata(),
    @SerialName("check_is_image")
    val checkIsImage: Boolean = OcrQueueStagingOptions.DEFAULTS.checkIsImage,
    @SerialName("check_is_arcaea_image")
    val checkIsArcaeaImage: Boolean = OcrQueueStagingOptions.DEFAULTS.checkIsArcaeaImage,
    @SerialName("parallel_count")
    val parallelCount: Int = (Runtime.getRuntime().availableProcessors() / 2).coerceAtLeast(1),
)

object OcrQueuePreferencesSerializer : OkioSerializer<OcrQueuePreferences> {
    override val defaultValue: OcrQueuePreferences = OcrQueuePreferences()

    override suspend fun readFrom(source: BufferedSource): OcrQueuePreferences =
        try {
            Toml.decodeFromString<OcrQueuePreferences>(source.readUtf8())
        } catch (e: CancellationException) {
            throw e
        } catch (exception: Exception) {
            throw CorruptionException("Cannot read OcrQueuePreferences from TOML file", exception)
        }

    override suspend fun writeTo(
        t: OcrQueuePreferences,
        sink: BufferedSink,
    ) {
        sink.writeUtf8(Toml.encodeToString<OcrQueuePreferences>(t))
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
        if (value < 1) throw IllegalArgumentException("Parallel count must be greater than 0")
        dataStore.updateData { preferences ->
            preferences.copy(parallelCount = value)
        }
    }
}
