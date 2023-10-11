package xyz.sevive.arcaeaoffline.ocr

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import java.util.Date

/**
 * Perceptual Hash computation.
 * Implementation follows http://www.hackerfactor.com/blog/index.php?/archives/432-Looks-Like-It.html
 * Adapted from `imagehash.phash`, pure opencv implementation
 * The result is slightly different from `imagehash.phash`.
 */
fun calculatePhash(imgGray: Mat, hashSize: Int = 8, highfreqFactor: Int = 4): Mat {
    assert(hashSize >= 2)

    val imgSize = hashSize * highfreqFactor
    val img = Mat()
    Imgproc.resize(
        imgGray, img, Size(imgSize.toDouble(), imgSize.toDouble()), 0.0, 0.0, Imgproc.INTER_LANCZOS4
    )
    img.convertTo(img, CvType.CV_32FC1)
    val dct = Mat()
    Core.dct(img, dct)
    val dctLowfreq = dct.submat(0, hashSize, 0, hashSize).clone()
    val med = matMedian(dctLowfreq.clone())
    val diff = Mat()
    Core.compare(dctLowfreq, Scalar(med), diff, Core.CMP_GT)
    return diff
}

fun matToBooleanArray(mat: Mat): BooleanArray {
    val arrSize = mat.rows() * mat.cols()
    val arr = mat.clone().reshape(1, arrSize)
    return BooleanArray(arrSize) { arr.get(it, 0)[0].toInt().toByte() != 0.toByte() }
}

class ImagePhashDatabase(path: String) {
    val dbObj: SQLiteDatabase =
        SQLiteDatabase.openDatabase(path, null, SQLiteDatabase.OPEN_READONLY)

    private var pHashSize = 0
    private var pHighfreqFactor = 0
    private var pBuiltDate = Date()

    val hashSize: Int get() = pHashSize
    val highfreqFactor: Int get() = pHighfreqFactor
    val builtTime: Date get() = pBuiltDate

    val ids = mutableListOf<String>()
    val hashes = mutableListOf<BooleanArray>()

    val jacketIds = mutableListOf<String>()
    val jacketHashes = mutableListOf<BooleanArray>()
    val partnerIconIds = mutableListOf<String>()
    val partnerIconHashes = mutableListOf<BooleanArray>()

    private fun getColumnIndex(cursor: Cursor, columnName: String): Int {
        val index = cursor.getColumnIndex(columnName)
        if (index > -1) return index
        else throw Error("Invalid pHash database: cannot get column $columnName")
    }

    init {
        val propertiesCursor =
            dbObj.query("properties", arrayOf("key", "value"), null, null, null, null, null)

        if (propertiesCursor.count <= 0) {
            propertiesCursor.close()
            throw Error("Invalid pHash database: `properties` table not found")
        } else {
            propertiesCursor.moveToFirst()

            val keyIndex = getColumnIndex(propertiesCursor, "key")
            val valueIndex = getColumnIndex(propertiesCursor, "value")

            for (i in 0 until propertiesCursor.count) {
                val propertyName = propertiesCursor.getString(keyIndex)

                if (propertyName == "hash_size") {
                    pHashSize = propertiesCursor.getInt(valueIndex)
                } else if (propertyName == "highfreq_factor") {
                    pHighfreqFactor = propertiesCursor.getInt(valueIndex)
                } else if (propertyName == "built_timestamp") {
                    val unixTimestamp = propertiesCursor.getInt(valueIndex).toLong()
                    pBuiltDate = Date(unixTimestamp * 1000)
                }

                propertiesCursor.moveToNext()
            }
        }

        val hashesCursor =
            dbObj.query("hashes", arrayOf("id", "hash"), null, null, null, null, null)

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
            val idSplitted = id.split("||")
            if (idSplitted.size > 1 && idSplitted[0] == "partner_icon") {
                partnerIconIds.add(idSplitted[1])
                partnerIconHashes.add(hash)
            } else {
                jacketIds.add(id)
                jacketHashes.add(hash)
            }
        }
    }

    fun xorBooleanArray(arr1: BooleanArray, arr2: BooleanArray): Int {
        assert(arr1.size == arr2.size)
        return arr1.zip(arr2).count { it.first xor it.second }
    }

    fun calculateImagePhash(imgGray: Mat) =
        calculatePhash(imgGray, this.hashSize, this.highfreqFactor)

    /**
     * This function takes a boolean array as a hash and a list of boolean arrays as hashes to
     * compare with.
     * @return A pair of integers, where the first element is the index of the hash in the list
     *         that has the smallest Hamming distance with the given hash, and the second element
     *         is the value of that distance.
     */
    fun lookupHashes(
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

    fun lookupImages(imgGray: Mat) = lookupImagesHelper(imgGray, this.ids, this.hashes)
    fun lookupImage(imgGray: Mat) = lookupImages(imgGray)[0]

    fun lookupJackets(imgGray: Mat) = lookupImagesHelper(imgGray, this.jacketIds, this.jacketHashes)
    fun lookupJacket(imgGray: Mat) = lookupJackets(imgGray)[0]

    fun lookupPartnerIcons(imgGray: Mat) =
        lookupImagesHelper(imgGray, this.partnerIconIds, this.partnerIconHashes)

    fun lookupPartnerIcon(imgGray: Mat) = lookupPartnerIcons(imgGray)[0]
}