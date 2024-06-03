package xyz.sevive.arcaeaoffline.core.database.converters

import androidx.room.TypeConverter
import java.nio.ByteBuffer
import java.util.UUID


/**
 * @see <a href="https://www.baeldung.com/java-byte-array-to-uuid">Convert Between Byte Array and UUID in Java</a>
 */
object UUIDByteArrayConverters {
    @TypeConverter
    fun fromDatabaseValue(value: ByteArray?): UUID? {
        return value?.let {
            val byteBuffer = ByteBuffer.wrap(it)
            UUID(byteBuffer.getLong(), byteBuffer.getLong())
        }
    }

    @TypeConverter
    fun toDatabaseValue(uuid: UUID?): ByteArray? {
        return uuid?.let {
            val bb = ByteBuffer.wrap(ByteArray(16))
            bb.putLong(it.mostSignificantBits)
            bb.putLong(it.leastSignificantBits)
            bb.array()
        }
    }
}
