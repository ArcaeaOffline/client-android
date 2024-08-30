package xyz.sevive.arcaeaoffline.database.converters

import androidx.room.TypeConverter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream


object ExceptionConverters {
    @TypeConverter
    fun fromDatabaseValue(value: ByteArray?): Exception? {
        return value?.let {
            runCatching {
                val objectInputStream = ObjectInputStream(ByteArrayInputStream(it))

                objectInputStream.readObject() as Exception
            }.getOrNull()
        }
    }

    @TypeConverter
    fun toDatabaseValue(value: Exception?): ByteArray? {
        return value?.let {
            runCatching {
                val byteArrayOutputStream = ByteArrayOutputStream()
                val objectOutputStream = ObjectOutputStream(byteArrayOutputStream)
                objectOutputStream.writeObject(it)
                objectOutputStream.flush()

                byteArrayOutputStream.toByteArray()
            }.getOrNull()
        }
    }
}
