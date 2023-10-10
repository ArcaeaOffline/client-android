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

fun calculatePhash(imgGray: Mat, hashSize: Int = 8, highfreqFactor: Int = 4): Mat {
    // Perceptual Hash computation.
    // Implementation follows http://www.hackerfactor.com/blog/index.php?/archives/432-Looks-Like-It.html
    // Adapted from `imagehash.phash`, pure opencv implementation
    // The result is slightly different from `imagehash.phash`.

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

    val ids: MutableList<String> = mutableListOf()
    val hashes: MutableList<BooleanArray> = mutableListOf()

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
    }

    fun xorBooleanArray(arr1: BooleanArray, arr2: BooleanArray): Int {
        assert(arr1.size == arr2.size)
        return arr1.zip(arr2).count { it.first xor it.second }
    }

    fun lookupImage(imgGray: Mat): Pair<String, Int> {
        val pHashMat = calculatePhash(imgGray, this.hashSize, this.highfreqFactor)
        val pHashBoolArr = matToBooleanArray(pHashMat)

        var minIndex = -1
        var minDiff = -1
        for (boolArr in this.hashes.withIndex()) {
            val diff = xorBooleanArray(pHashBoolArr, boolArr.value)
            if (minDiff < 0 || diff < minDiff) {
                minDiff = diff
                minIndex = boolArr.index
            }
        }

        return Pair(this.ids[minIndex], minDiff)
    }
}