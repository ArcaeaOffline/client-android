package xyz.sevive.arcaeaoffline.core.ocr

import android.util.Log
import io.requery.android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.opencv.core.Mat
import org.threeten.bp.Instant
import java.io.File

class ImageHashesDatabaseBuilder(private val db: SQLiteDatabase) {
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

    private val stmtInsertProperty by lazy {
        db.compileStatement("INSERT INTO properties (`key`, `value`) VALUES (?, ?)")
    }
    private val stmtInsertHash by lazy {
        db.compileStatement("INSERT INTO hashes (`hash_type`, `type`, `label`, `hash`) VALUES (?, ?, ?, ?)")
    }

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

    private fun insertProperty(key: String, value: String) {
        stmtInsertProperty.clearBindings()
        stmtInsertProperty.bindString(1, key)
        stmtInsertProperty.bindString(2, value)
        stmtInsertProperty.execute()
    }

    private fun insertHash(
        hashType: ImageHashItemHashType, type: ImageHashItemType, label: String, hash: Mat
    ) {
        stmtInsertHash.clearBindings()
        stmtInsertHash.bindLong(1, hashType.value.toLong())
        stmtInsertHash.bindLong(2, type.value.toLong())
        stmtInsertHash.bindString(3, label)
        stmtInsertHash.bindBlob(4, hash.toHashByteArray())
        stmtInsertHash.execute()
    }

    private fun insertProperties(hashSize: Int, highFreqFactor: Int, builtTimestamp: Instant) {
        insertProperty(ImageHashesDatabase.PROP_HASH_SIZE_KEY, hashSize.toString())
        insertProperty(ImageHashesDatabase.PROP_HIGH_FREQ_FACTOR_KEY, highFreqFactor.toString())
        insertProperty(
            ImageHashesDatabase.PROP_BUILT_TIMESTAMP_KEY, builtTimestamp.toEpochMilli().toString()
        )
    }

    private fun createTables() {
        db.execSQL("CREATE TABLE properties (`key` VARCHAR, `value` VARCHAR)")
        db.execSQL("CREATE TABLE hashes (`hash_type` INTEGER, `type` INTEGER, `label` VARCHAR, `hash` BLOB)")
    }

    fun build(hashSize: Int, highFreqFactor: Int) {
        Log.d(LOG_TAG, "build() called, ${tasks.size} items in task list")
        db.beginTransaction()

        try {
            initBuildProgress()

            createTables()

            tasks.forEach { task ->
                val img = task.inputToGrayscaleImage(task.input)

                mapOf(
                    ImageHashItemHashType.AVERAGE to ImageHashers.average(img, hashSize),
                    ImageHashItemHashType.DIFFERENCE to ImageHashers.difference(img, hashSize),
                    ImageHashItemHashType.DCT to ImageHashers.dct(img, hashSize, highFreqFactor)
                ).forEach {
                    insertHash(
                        hashType = it.key, type = task.type, label = task.label, hash = it.value
                    )
                }

                increaseBuildProgress()
            }

            insertProperties(
                hashSize = hashSize,
                highFreqFactor = highFreqFactor,
                builtTimestamp = Instant.now(),
            )

            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
            resetBuildProgress()
        }
    }
}
