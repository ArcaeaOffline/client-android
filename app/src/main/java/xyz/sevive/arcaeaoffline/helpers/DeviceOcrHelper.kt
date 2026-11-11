package xyz.sevive.arcaeaoffline.helpers

import ai.onnxruntime.OrtSession
import android.content.Context
import android.net.Uri
import de.stefan_oltmann.kim.Kim
import de.stefan_oltmann.kim.format.tiff.constant.ExifTag
import de.stefan_oltmann.kim.input.ByteReader
import de.stefan_oltmann.kim.input.KotlinIoSourceByteReader
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.size
import io.github.vinceglb.filekit.source
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.parseOrNull
import kotlinx.datetime.toInstant
import kotlinx.io.buffered
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrScenario
import xyz.sevive.arcaeaoffline.core.ocr.device.toPlayResult
import xyz.sevive.arcaeaoffline.core.ocr.opencv.use
import xyz.sevive.arcaeaoffline.helpers.context.getFilename
import kotlin.time.Instant

object DeviceOcrHelper {
    private val exifTagLocalDateTimeFormat =
        LocalDateTime.Format {
            year()
            char(':')
            monthNumber()
            char(':')
            day()
            char(' ')
            hour()
            char(':')
            minute()
            char(':')
            second()
        }

    suspend fun ocrImage(
        imageUri: Uri,
        imageHashesDatabase: ImageHashesDatabase,
        ortSession: OrtSession,
    ): DeviceOcrResult {
        val byteArray = PlatformFile(imageUri).readBytes()

        return MatOfByte(*byteArray).use { matOfBytes ->
            Imgcodecs.imdecode(matOfBytes, Imgcodecs.IMREAD_COLOR).use { img ->
                DeviceOcrScenario.forImage(img).use { scenario ->
                    scenario.ocr(ortSession, imageHashesDatabase)
                }
            }
        }
    }

    fun readImageDateFromExif(
        byteReader: ByteReader,
        overrideDate: Instant? = null,
        defaultTimeZoneProvider: () -> TimeZone = { TimeZone.currentSystemDefault() },
    ): Instant? {
        if (overrideDate != null) return overrideDate

        val metadata = Kim.readMetadata(byteReader) ?: return null

        val dateTimeOriginal = metadata.findStringValue(ExifTag.EXIF_TAG_DATE_TIME_ORIGINAL) ?: return null
        val localDateTime = exifTagLocalDateTimeFormat.parseOrNull(dateTimeOriginal) ?: return null
        val exifTimeZone =
            metadata.findStringValue(ExifTag.EXIF_TAG_OFFSET_TIME_ORIGINAL)?.let {
                val zoneOffset = UtcOffset.parseOrNull(it)
                zoneOffset?.asTimeZone()
            }

        return localDateTime.toInstant(exifTimeZone ?: defaultTimeZoneProvider())
    }

    fun readImageDateFromExif(
        imageFile: PlatformFile,
        overrideDate: Instant? = null,
        defaultTimeZoneProvider: () -> TimeZone = { TimeZone.currentSystemDefault() },
    ): Instant? =
        readImageDateFromExif(
            byteReader = KotlinIoSourceByteReader(imageFile.source().buffered(), imageFile.size()),
            overrideDate = overrideDate,
            defaultTimeZoneProvider = defaultTimeZoneProvider,
        )

    fun readImageDateFromExif(
        imageUri: Uri,
        overrideDate: Instant? = null,
        defaultTimeZoneProvider: () -> TimeZone = { TimeZone.currentSystemDefault() },
    ): Instant? =
        readImageDateFromExif(
            imageFile = PlatformFile(imageUri),
            overrideDate = overrideDate,
            defaultTimeZoneProvider = defaultTimeZoneProvider,
        )

    fun ocrResultToPlayResult(
        imageUri: Uri,
        context: Context,
        ocrResult: DeviceOcrResult,
        fallbackDate: Instant? = null,
        overrideDate: Instant? = null,
    ): PlayResult {
        val date = readImageDateFromExif(imageUri, overrideDate) ?: fallbackDate
        val imgFilename = context.getFilename(imageUri)

        return ocrResult.toPlayResult(
            date = date,
            comment = if (imgFilename != null) "OCR $imgFilename" else null,
        )
    }
}
