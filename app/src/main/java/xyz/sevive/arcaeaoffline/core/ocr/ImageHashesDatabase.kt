package xyz.sevive.arcaeaoffline.core.ocr

import io.requery.android.database.sqlite.SQLiteDatabase
import org.opencv.core.Mat
import org.threeten.bp.Instant
import kotlin.math.pow
import kotlin.properties.Delegates

class ImageHashesDatabase(private val db: SQLiteDatabase) {
    companion object {
        const val PROP_HASH_SIZE_KEY = "hash_size"
        const val PROP_HIGH_FREQ_FACTOR_KEY = "high_freq_factor"
        const val PROP_BUILT_TIMESTAMP_KEY = "built_timestamp"
    }

    private var hashSize: Int by Delegates.notNull()
    private var highFreqFactor: Int by Delegates.notNull()
    private var builtTime: Instant by Delegates.notNull()

    private fun initialize() {
        val cursor = db.rawQuery("SELECT key, value FROM properties", null)

        cursor.use {
            cursor.moveToFirst()
            do {
                val key = cursor.getString(0)
                val value = cursor.getString(1)
                when (key) {
                    PROP_HASH_SIZE_KEY -> hashSize = value.toInt()
                    PROP_HIGH_FREQ_FACTOR_KEY -> highFreqFactor = value.toInt()
                    PROP_BUILT_TIMESTAMP_KEY -> builtTime = Instant.ofEpochMilli(value.toLong())
                }
            } while (cursor.moveToNext())
        }

        db.addFunction("HAMMING_DISTANCE", 2) { args, result ->
            val byteArray1 = args.getBlob(0)
            val byteArray2 = args.getBlob(1)
            assert(byteArray1.size == byteArray2.size) { "hash size does not match!" }
            val count = byteArray1.zip(byteArray2).count { (b1, b2) -> b1 != b2 }
            result.set(count)
        }
    }

    init {
        initialize()
    }

    private fun lookupHash(
        type: ImageHashItemType, hashType: ImageHashItemHashType, hash: ByteArray
    ): List<ImageHashItem> {
        val cursor = db.rawQuery(
            """SELECT label, HAMMING_DISTANCE(hash, ?) AS distance FROM hashes
                WHERE type = ? AND hash_type = ?
                ORDER BY distance ASC LIMIT 10""".trimIndent(),
            arrayOf(hash, type.value, hashType.value)
        )

        val result = mutableListOf<ImageHashItem>()
        cursor.use {
            it.moveToFirst()
            do {
                val label = it.getString(0)
                val distance = it.getInt(1)

                val hashLength = hashSize.toDouble().pow(2)
                result.add(
                    ImageHashItem(
                        hashType = hashType,
                        type = type,
                        label = label,
                        confidence = (hashLength - distance) / hashLength
                    )
                )
            } while (it.moveToNext())
        }
        return result
    }

    private fun lookupAHash(type: ImageHashItemType, hash: ByteArray) =
        lookupHash(type = type, hashType = ImageHashItemHashType.AVERAGE, hash = hash)

    private fun lookupDHash(type: ImageHashItemType, hash: ByteArray) =
        lookupHash(type = type, hashType = ImageHashItemHashType.DIFFERENCE, hash = hash)

    private fun lookupPHash(type: ImageHashItemType, hash: ByteArray) =
        lookupHash(type = type, hashType = ImageHashItemHashType.DCT, hash = hash)

    private fun lookupImage(type: ImageHashItemType, image: Mat): List<ImageHashItem> {
        val items = mutableListOf<ImageHashItem>()

        val aHash = ImageHashers.average(image, this.hashSize)
        val dHash = ImageHashers.difference(image, this.hashSize)
        val pHash = ImageHashers.dct(image, this.hashSize, this.highFreqFactor)

        items.addAll(lookupAHash(type, aHash.toHashByteArray()))
        items.addAll(lookupDHash(type, dHash.toHashByteArray()))
        items.addAll(lookupPHash(type, pHash.toHashByteArray()))

        return items
    }

    fun lookupJacket(image: Mat): List<ImageHashItem> =
        lookupImage(type = ImageHashItemType.JACKET, image = image)

    fun lookupPartnerIcon(image: Mat): List<ImageHashItem> =
        lookupImage(type = ImageHashItemType.PARTNER_ICON, image = image)
}
