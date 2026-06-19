package xyz.sevive.arcaeaoffline.core.ocr

import androidx.sqlite.SQLiteConnection
import org.opencv.core.Mat
import java.time.Instant
import kotlin.math.pow
import kotlin.properties.Delegates

private fun hammingDistance(
    byteArray1: ByteArray,
    byteArray2: ByteArray,
): Int {
    assert(byteArray1.size == byteArray2.size) { "hash size does not match!" }
    return byteArray1.zip(byteArray2).count { (b1, b2) -> b1 != b2 }
}

class ImageHashesDatabase(
    private val conn: SQLiteConnection,
) {
    companion object {
        const val PROP_HASH_SIZE_KEY = "hash_size"
        const val PROP_HIGH_FREQ_FACTOR_KEY = "high_freq_factor"
        const val PROP_BUILT_TIMESTAMP_KEY = "built_timestamp"
    }

    private var hashSize: Int by Delegates.notNull()
    private var highFreqFactor: Int by Delegates.notNull()
    var builtTime: Instant by Delegates.notNull()
        private set
    var jacketHashesCount: Int by Delegates.notNull()
        private set
    var partnerIconHashesCount: Int by Delegates.notNull()
        private set
    val hashesCount: Int get() = jacketHashesCount + partnerIconHashesCount

    private class HashEntry(
        val type: ImageHashItemType,
        val hashType: ImageHashItemHashType,
        val label: String,
        val hash: ByteArray,
    ) {
        fun distance(otherHash: ByteArray) = hammingDistance(this.hash, otherHash)
    }

    private lateinit var allHashes: List<HashEntry>

    private fun initialize() {
        conn.prepare("SELECT `key`, `value` FROM `properties`").use { stmt ->
            while (stmt.step()) {
                val key = stmt.getText(0)
                val value = stmt.getText(1)

                when (key) {
                    PROP_HASH_SIZE_KEY -> hashSize = value.toInt()
                    PROP_HIGH_FREQ_FACTOR_KEY -> highFreqFactor = value.toInt()
                    PROP_BUILT_TIMESTAMP_KEY -> builtTime = Instant.ofEpochMilli(value.toLong())
                }
            }
        }

        val hashes = mutableListOf<HashEntry>()
        conn.prepare("SELECT `hash_type`, `type`, `label`, `hash` FROM `hashes`").use { stmt ->
            while (stmt.step()) {
                hashes.add(
                    HashEntry(
                        type = ImageHashItemType.entries[stmt.getInt(1)],
                        hashType = ImageHashItemHashType.entries[stmt.getInt(0)],
                        label = stmt.getText(2),
                        hash = stmt.getBlob(3),
                    ),
                )
            }
        }
        allHashes = hashes

        jacketHashesCount =
            hashes.filter { it.type == ImageHashItemType.JACKET }.distinctBy { it.label }.count()
        partnerIconHashesCount =
            hashes
                .filter { it.type == ImageHashItemType.PARTNER_ICON }
                .distinctBy { it.label }
                .count()
    }

    init {
        initialize()
    }

    @Suppress("Unused")
    private fun lookupHashWithSqlFunc(
        type: ImageHashItemType,
        hashType: ImageHashItemHashType,
        hash: ByteArray,
    ): List<ImageHashItem> {
//        conn.addFunction("HAMMING_DISTANCE", 2) { args, result ->
//            val byteArray1 = args.getBlob(0)
//            val byteArray2 = args.getBlob(1)
//            result.set(hammingDistance(byteArray1, byteArray2))
//        }

        val result = mutableListOf<ImageHashItem>()

        conn
            .prepare(
                """
                SELECT label, HAMMING_DISTANCE(hash, ?) AS distance FROM hashes
                WHERE type = ? AND hash_type = ?
                ORDER BY distance ASC LIMIT 10
                """.trimIndent(),
            ).use { stmt ->
                stmt.bindBlob(1, hash)
                stmt.bindInt(2, type.value)
                stmt.bindInt(3, hashType.value)

                while (stmt.step()) {
                    val label = stmt.getText(0)
                    val distance = stmt.getInt(1)

                    val hashLength = hashSize.toDouble().pow(2)
                    result.add(
                        ImageHashItem(
                            hashType = hashType,
                            type = type,
                            label = label,
                            confidence = (hashLength - distance) / hashLength,
                        ),
                    )
                }
            }

        return result
    }

    private fun lookupHash(
        type: ImageHashItemType,
        hashType: ImageHashItemHashType,
        hash: ByteArray,
    ): List<ImageHashItem> {
        val hashLength = hashSize.toDouble().pow(2)
        return allHashes
            .filter { it.type == type && it.hashType == hashType }
            .sortedBy { it.distance(hash) }
            .take(10)
            .map { entry ->
                val distance = entry.distance(hash)

                ImageHashItem(
                    hashType = hashType,
                    type = type,
                    label = entry.label,
                    confidence = (hashLength - distance) / hashLength,
                )
            }
    }

    private fun lookupAHash(
        type: ImageHashItemType,
        hash: ByteArray,
    ) = lookupHash(type = type, hashType = ImageHashItemHashType.AVERAGE, hash = hash)

    private fun lookupDHash(
        type: ImageHashItemType,
        hash: ByteArray,
    ) = lookupHash(type = type, hashType = ImageHashItemHashType.DIFFERENCE, hash = hash)

    private fun lookupPHash(
        type: ImageHashItemType,
        hash: ByteArray,
    ) = lookupHash(type = type, hashType = ImageHashItemHashType.DCT, hash = hash)

    private fun lookupImage(
        type: ImageHashItemType,
        image: Mat,
    ): List<ImageHashItem> {
        val items = mutableListOf<ImageHashItem>()

        val aHash = ImageHashers.average(image, this.hashSize)
        val dHash = ImageHashers.difference(image, this.hashSize)
        val pHash = ImageHashers.dct(image, this.hashSize, this.highFreqFactor)

        items.addAll(lookupAHash(type, aHash.toHashByteArray()))
        items.addAll(lookupDHash(type, dHash.toHashByteArray()))
        items.addAll(lookupPHash(type, pHash.toHashByteArray()))

        return items
    }

    fun lookupJacket(image: Mat): List<ImageHashItem> = lookupImage(type = ImageHashItemType.JACKET, image = image)

    fun lookupPartnerIcon(image: Mat): List<ImageHashItem> = lookupImage(type = ImageHashItemType.PARTNER_ICON, image = image)
}
