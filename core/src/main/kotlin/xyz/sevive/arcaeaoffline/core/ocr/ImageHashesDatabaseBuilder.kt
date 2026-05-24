package xyz.sevive.arcaeaoffline.core.ocr

import android.util.Log
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.opencv.core.Mat
import org.threeten.bp.Instant
import java.io.File

class ImageHashesDatabaseBuilder(private val conn: SQLiteConnection) {
    companion object {
        const val LOG_TAG = "ImageHashesDbBuilder"
    }

    data class Task<T>(
        val type: ImageHashItemType,
        val label: String,
        val input: T,
        val inputToGrayscaleImage: (T) -> Mat
    )

    private val _buildProgress = MutableStateFlow<Pair<Int, Int>?>(null)
    val buildProgress = _buildProgress.asStateFlow()

    private val tasks = mutableListOf<Task<File>>()

    private fun initBuildProgress() {
        _buildProgress.value = 0 to tasks.size
    }

    private fun increaseBuildProgress() {
        _buildProgress.value = _buildProgress.value?.let { it.copy(first = it.first + 1) }
    }

    private fun resetBuildProgress() {
        _buildProgress.value = null
    }

    fun addTask(
        type: ImageHashItemType, label: String, input: File, inputToGrayscaleImage: (File) -> Mat
    ) {
        tasks.add(Task(type, label, input, inputToGrayscaleImage))
    }

    private fun insertProperties(hashSize: Int, highFreqFactor: Int, builtTimestamp: Instant) {
        conn.prepare("INSERT INTO `properties` (`key`, `value`) VALUES (?, ?)").use { stmt ->
            mapOf(
                ImageHashesDatabase.PROP_HASH_SIZE_KEY to hashSize.toString(),
                ImageHashesDatabase.PROP_HIGH_FREQ_FACTOR_KEY to highFreqFactor.toString(),
                ImageHashesDatabase.PROP_BUILT_TIMESTAMP_KEY to builtTimestamp.toEpochMilli()
                    .toString(),
            ).forEach { (key, value) ->
                stmt.bindText(1, key)
                stmt.bindText(2, value)

                stmt.step()
                stmt.clearBindings()
                stmt.reset()
            }
        }
    }

    private fun createTables() {
        conn.execSQL("CREATE TABLE properties (`key` VARCHAR, `value` VARCHAR)")
        conn.execSQL("CREATE TABLE hashes (`hash_type` INTEGER, `type` INTEGER, `label` VARCHAR, `hash` BLOB)")
    }

    fun build(hashSize: Int, highFreqFactor: Int) {
        Log.d(LOG_TAG, "build() called, ${tasks.size} items in task list")
        conn.execSQL("BEGIN IMMEDIATE TRANSACTION")

        try {
            initBuildProgress()

            createTables()

            conn.prepare("INSERT INTO `hashes` (`hash_type`, `type`, `label`, `hash`) VALUES (?, ?, ?, ?)")
                .use { stmtInsertHash ->
                    tasks.forEach { task ->
                        val img = task.inputToGrayscaleImage(task.input)

                        mapOf(
                            ImageHashItemHashType.AVERAGE to ImageHashers.average(img, hashSize),
                            ImageHashItemHashType.DIFFERENCE to ImageHashers.difference(
                                img,
                                hashSize
                            ),
                            ImageHashItemHashType.DCT to ImageHashers.dct(
                                img,
                                hashSize,
                                highFreqFactor
                            )
                        ).forEach { (hashType, hash) ->
                            stmtInsertHash.bindInt(1, hashType.value)
                            stmtInsertHash.bindInt(2, task.type.value)
                            stmtInsertHash.bindText(3, task.label)
                            stmtInsertHash.bindBlob(4, hash.toHashByteArray())
                            stmtInsertHash.step()
                            stmtInsertHash.clearBindings()
                            stmtInsertHash.reset()
                        }

                        increaseBuildProgress()
                    }
                }

            insertProperties(
                hashSize = hashSize,
                highFreqFactor = highFreqFactor,
                builtTimestamp = Instant.now(),
            )

            conn.execSQL("COMMIT")
        } catch (e: Exception) {
            Log.e(LOG_TAG, "Error building ihdb", e)
            conn.execSQL("ROLLBACK")
        } finally {
            resetBuildProgress()
        }
    }
}
