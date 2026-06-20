package xyz.sevive.arcaeaoffline.helpers

import ai.onnxruntime.OrtSession
import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.source
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import kotlinx.datetime.format.char
import kotlinx.datetime.parseOrNull
import kotlinx.datetime.toInstant
import kotlinx.io.asInputStream
import kotlinx.io.buffered
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.core.ArcaeaPartnerModifiers
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.CropBlackEdges
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcr
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.DeviceRoisAutoSelector
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.DeviceRoisAutoSelectorResult
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT1
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT2
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.extractor.DeviceRoisExtractor
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceRoisMaskerAutoT1
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceRoisMaskerAutoT2
import xyz.sevive.arcaeaoffline.core.ocr.device.toPlayResult
import xyz.sevive.arcaeaoffline.helpers.context.getFilename
import java.io.InputStream
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
        kNearestModel: KNearest,
        imageHashesDatabase: ImageHashesDatabase,
        ortSession: OrtSession,
    ): DeviceOcrResult {
        val byteArray = PlatformFile(imageUri).readBytes()
        val img = Imgcodecs.imdecode(MatOfByte(*byteArray), Imgcodecs.IMREAD_COLOR)
        val imgCropped = CropBlackEdges.crop(img)

        val roisAutoType = DeviceRoisAutoSelector.select(img)
        val rois =
            when (roisAutoType) {
                DeviceRoisAutoSelectorResult.T1 -> {
                    DeviceRoisAutoT1(
                        imgCropped.width(),
                        imgCropped.height(),
                    )
                }

                else -> {
                    DeviceRoisAutoT2(
                        imgCropped.width(),
                        imgCropped.height(),
                    )
                }
            }
        val extractor = DeviceRoisExtractor(rois, imgCropped)
        val masker =
            when (roisAutoType) {
                DeviceRoisAutoSelectorResult.T1 -> DeviceRoisMaskerAutoT1()
                else -> DeviceRoisMaskerAutoT2()
            }

        return DeviceOcr(
            extractor = extractor,
            masker = masker,
            kNearestModel = kNearestModel,
            ortSession = ortSession,
            hashesDb = imageHashesDatabase,
        ).ocr()
    }

    fun readImageDateFromExif(
        imageInputStream: InputStream,
        fallbackDate: Instant? = null,
        overrideDate: Instant? = null,
        defaultTimeZoneProvider: () -> TimeZone = { TimeZone.currentSystemDefault() },
    ): Instant? {
        if (overrideDate != null) return overrideDate

        val imgExif = ExifInterface(imageInputStream)
        val imgExifDateTimeOriginal =
            imgExif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL) ?: return fallbackDate

        val localDateTime = exifTagLocalDateTimeFormat.parseOrNull(imgExifDateTimeOriginal) ?: return null

        val exifTimeZone =
            imgExif.getAttribute(ExifInterface.TAG_OFFSET_TIME_ORIGINAL)?.let {
                val zoneOffset = UtcOffset.parseOrNull(it)
                zoneOffset?.asTimeZone()
            }

        return localDateTime.toInstant(exifTimeZone ?: defaultTimeZoneProvider())
    }

    fun readImageDateFromExif(
        imageFile: PlatformFile,
        fallbackDate: Instant? = null,
        overrideDate: Instant? = null,
        defaultTimeZoneProvider: () -> TimeZone = { TimeZone.currentSystemDefault() },
    ): Instant? =
        readImageDateFromExif(
            imageInputStream = imageFile.source().buffered().asInputStream(),
            fallbackDate = fallbackDate,
            overrideDate = overrideDate,
            defaultTimeZoneProvider = defaultTimeZoneProvider,
        )

    fun readImageDateFromExif(
        imageUri: Uri,
        fallbackDate: Instant? = null,
        overrideDate: Instant? = null,
        defaultTimeZoneProvider: () -> TimeZone = { TimeZone.currentSystemDefault() },
    ): Instant? =
        readImageDateFromExif(
            imageFile = PlatformFile(imageUri),
            fallbackDate = fallbackDate,
            overrideDate = overrideDate,
            defaultTimeZoneProvider = defaultTimeZoneProvider,
        )

    fun ocrResultToPlayResult(
        imageUri: Uri,
        context: Context,
        ocrResult: DeviceOcrResult,
        fallbackDate: Instant? = null,
        overrideDate: Instant? = null,
        customArcaeaPartnerModifiers: ArcaeaPartnerModifiers? = null,
    ): PlayResult {
        val arcaeaPartnerModifiers =
            customArcaeaPartnerModifiers ?: ArcaeaPartnerModifiers(context.assets)

        val date = readImageDateFromExif(imageUri, fallbackDate, overrideDate)
        val imgFilename = context.getFilename(imageUri)

        return ocrResult.toPlayResult(
            arcaeaPartnerModifiers = arcaeaPartnerModifiers,
            date = date,
            comment = if (imgFilename != null) "OCR $imgFilename" else null,
        )
    }
}
