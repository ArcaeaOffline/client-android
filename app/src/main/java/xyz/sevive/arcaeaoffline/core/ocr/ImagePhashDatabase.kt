package xyz.sevive.arcaeaoffline.core.ocr

import android.content.ContentValues
import android.database.Cursor
import io.requery.android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.threeten.bp.Instant
import java.io.File

/**
 * Perceptual Hash computation.
 * Implementation follows http://www.hackerfactor.com/blog/index.php?/archives/432-Looks-Like-It.html
 * Adapted from `imagehash.phash`, pure opencv implementation
 * The result is slightly different from `imagehash.phash`.
 */
fun calculatePhash(imgGray: Mat, hashSize: Int = 8, highFreqFactor: Int = 4): Mat {
    assert(hashSize >= 2)

    val imgSize = hashSize * highFreqFactor
    val img = Mat()
    Imgproc.resize(
        imgGray, img, Size(imgSize.toDouble(), imgSize.toDouble()), 0.0, 0.0, Imgproc.INTER_LANCZOS4
    )
    img.convertTo(img, CvType.CV_32FC1)
    val dct = Mat()
    Core.dct(img, dct)
    val dctLowFreq = dct.submat(0, hashSize, 0, hashSize).clone()
    val med = matMedian(dctLowFreq.clone())
    val diff = Mat()
    Core.compare(dctLowFreq, Scalar(med), diff, Core.CMP_GT)
    return diff
}

fun matToBooleanArray(mat: Mat): BooleanArray {
    val arrSize = mat.rows() * mat.cols()
    val arr = mat.clone().reshape(1, arrSize)
    return BooleanArray(arrSize) { arr.get(it, 0)[0].toInt().toByte() != 0.toByte() }
}

class ImagePhashDatabase(path: String) {
    var hashSize = 0
        private set
    private var highFreqFactor = 0
    var builtTime: Instant? = null
        private set

    private val ids = mutableListOf<String>()
    private val hashes = mutableListOf<BooleanArray>()

    private val jacketIds = mutableListOf<String>()
    val jacketHashes = mutableListOf<BooleanArray>()
    private val partnerIconIds = mutableListOf<String>()
    val partnerIconHashes = mutableListOf<BooleanArray>()

    private fun getColumnIndex(cursor: Cursor, columnName: String): Int {
        val index = cursor.getColumnIndex(columnName)
        if (index > -1) return index
        else throw Error("Invalid pHash database: cannot get column $columnName")
    }

    init {
        val db = SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)

        db.use {
            val propertiesCursor = db.query(
                "properties", arrayOf("key", "value"), null, null, null, null, null
            )

            if (propertiesCursor.count <= 0) {
                propertiesCursor.close()
                throw Error("Invalid pHash database: `properties` table not found")
            } else {
                propertiesCursor.moveToFirst()

                val keyIndex = getColumnIndex(propertiesCursor, "key")
                val valueIndex = getColumnIndex(propertiesCursor, "value")

                for (i in 0 until propertiesCursor.count) {
                    when (propertiesCursor.getString(keyIndex)) {
                        "hash_size" -> hashSize = propertiesCursor.getInt(valueIndex)
                        "highfreq_factor" -> highFreqFactor = propertiesCursor.getInt(valueIndex)
                        "built_timestamp" -> {
                            val unixTimestamp = propertiesCursor.getInt(valueIndex).toLong()
                            builtTime = Instant.ofEpochSecond(unixTimestamp)
                        }
                    }

                    propertiesCursor.moveToNext()
                }
            }

            val hashesCursor =
                db.query("hashes", arrayOf("id", "hash"), null, null, null, null, null)

            if (hashesCursor.count > 0) {
                hashesCursor.moveToFirst()

                val idIndex = getColumnIndex(hashesCursor, "id")
                val hashIndex = getColumnIndex(hashesCursor, "hash")

                for (i in 0 until hashesCursor.count) {
                    ids.add(hashesCursor.getString(idIndex))
                    val byteArray = hashesCursor.getBlob(hashIndex)
                    hashes.add(BooleanArray(byteArray.size) { byteArray[it] != 0.toByte() })

                    hashesCursor.moveToNext()
                }
            } else {
                hashesCursor.close()
                throw Error("Invalid pHash database: `hashes` table not found")
            }

            for ((id, hash) in ids.zip(hashes)) {
                val idSplit = id.split("||")
                if (idSplit.size > 1 && idSplit[0] == "partner_icon") {
                    partnerIconIds.add(idSplit[1])
                    partnerIconHashes.add(hash)
                } else {
                    jacketIds.add(id)
                    jacketHashes.add(hash)
                }
            }
        }
    }

    private fun calculateImagePhash(imgGray: Mat) =
        calculatePhash(imgGray, this.hashSize, this.highFreqFactor)

    /**
     * This function takes a boolean array as a hash and a list of boolean arrays as hashes to
     * compare with.
     * @return A pair of integers, where the first element is the index of the hash in the list
     *         that has the smallest Hamming distance with the given hash, and the second element
     *         is the value of that distance.
     */
    private fun lookupHashes(
        hash: BooleanArray, ids: List<String>, hashes: List<BooleanArray>, limit: Int = 5
    ): List<Pair<String, Int>> {
        val xorResults =
            ids.zip(hashes).map { (id, hashInArr) -> Pair(id, xorBooleanArray(hash, hashInArr)) }
        return xorResults.sortedBy { it.second }.subList(0, limit)
    }

    private fun lookupImagesHelper(
        imgGray: Mat, ids: List<String>, hashes: List<BooleanArray>
    ): List<Pair<String, Int>> {
        val phashMat = calculateImagePhash(imgGray)
        val hash = matToBooleanArray(phashMat)
        return lookupHashes(hash, ids, hashes)
    }

    private fun lookupJackets(imgGray: Mat) =
        lookupImagesHelper(imgGray, this.jacketIds, this.jacketHashes)

    fun lookupJacket(imgGray: Mat) = lookupJackets(imgGray)[0]

    private fun lookupPartnerIcons(imgGray: Mat) =
        lookupImagesHelper(imgGray, this.partnerIconIds, this.partnerIconHashes)

    fun lookupPartnerIcon(imgGray: Mat) = lookupPartnerIcons(imgGray)[0]

    companion object {
        fun xorBooleanArray(arr1: BooleanArray, arr2: BooleanArray): Int {
            assert(arr1.size == arr2.size)
            return arr1.zip(arr2).count { it.first xor it.second }
        }

        suspend fun build(
            databaseFile: File,
            images: List<Mat>,
            labels: List<String>,
            hashSize: Int = 16,
            highFreqFactor: Int = 4,
            progressCallback: (progress: Int, total: Int) -> Unit = { _, _ -> },
        ) {
            assert(images.size == labels.size) { "`images` and `labels` should have the same size" }

            var result = 0
            fun reportProgress() {
                result++
                progressCallback(result, images.size)
            }

            val labelResultMap = mutableMapOf<String, ByteArray>()

            withContext(Dispatchers.Default) {
                // calculate the phash of each image, then convert the boolean array to byte array
                // for later database insert convenience
                images.zip(labels).forEach { pair ->
                    val mat = pair.first
                    val label = pair.second

                    val hash = matToBooleanArray(calculatePhash(mat, hashSize, highFreqFactor))
                    val hashByteArray = ByteArray(hash.size)
                    hash.forEachIndexed { index, b ->
                        hashByteArray[index] = if (b) 1.toByte() else 0.toByte()
                    }

                    labelResultMap[label] = hashByteArray
                    reportProgress()
                }
            }

            SQLiteDatabase.openOrCreateDatabase(databaseFile, null).use { db ->
                db.execSQL("CREATE TABLE properties (`key` TEXT, value TEXT)")

                val hashSizeContentValues = ContentValues()
                hashSizeContentValues.put("key", "hash_size")
                hashSizeContentValues.put("value", hashSize)

                val highFreqFactorContentValues = ContentValues()
                highFreqFactorContentValues.put("key", "highfreq_factor")
                highFreqFactorContentValues.put("value", highFreqFactor)

                db.insert("properties", null, hashSizeContentValues)
                db.insert("properties", null, highFreqFactorContentValues)

                db.execSQL("CREATE TABLE hashes (id TEXT, hash BLOB(${hashSize * hashSize}))")
                db.execSQL("CREATE INDEX idx_hash_id ON hashes(id)")

                // bulk insert
                // https://stackoverflow.com/a/47642316/16484891, CC BY-SA 3.0
                val sql = "INSERT INTO hashes VALUES(?, ?)"
                val statement = db.compileStatement(sql)

                db.beginTransaction()
                labelResultMap.entries.forEach { entry ->
                    statement.clearBindings()
                    statement.bindString(1, entry.key)
                    statement.bindBlob(2, entry.value)
                    statement.execute()
                }
                db.setTransactionSuccessful()
                db.endTransaction()

                val buildTimestampContentValues = ContentValues()
                buildTimestampContentValues.put("key", "built_timestamp")
                buildTimestampContentValues.put("value", Instant.now().epochSecond)
                db.insert("properties", null, buildTimestampContentValues)
            }
        }
    }
}
