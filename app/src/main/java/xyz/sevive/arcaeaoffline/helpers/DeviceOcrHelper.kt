package xyz.sevive.arcaeaoffline.helpers

import android.content.Context
import android.net.Uri
import androidx.exifinterface.media.ExifInterface
import org.apache.commons.io.IOUtils
import org.opencv.core.MatOfByte
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.ml.KNearest
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset
import org.threeten.bp.format.DateTimeFormatter
import xyz.sevive.arcaeaoffline.core.ArcaeaPartnerModifiers
import xyz.sevive.arcaeaoffline.core.database.entities.Score
import xyz.sevive.arcaeaoffline.core.ocr.ImagePhashDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.CropBlackEdges
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcr
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrResult
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.DeviceRoisAutoSelector
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT1
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT2
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.extractor.DeviceRoisExtractor
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceRoisMaskerAutoT1
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceRoisMaskerAutoT2
import xyz.sevive.arcaeaoffline.core.ocr.device.toScore
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.helpers.context.getFilename
import java.io.FileNotFoundException

class DeviceOcrHelper {
    companion object {
        fun ocrImage(
            imageUri: Uri,
            context: Context,
            customKnnModel: KNearest? = null,
            customPhashDatabase: ImagePhashDatabase? = null,
        ): DeviceOcrResult {
            val ocrDependencyPaths = OcrDependencyPaths(context)

            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw FileNotFoundException("Cannot open a input stream for $imageUri")

            val byteArray = inputStream.use { IOUtils.toByteArray(inputStream) }
            val img = Imgcodecs.imdecode(MatOfByte(*byteArray), Imgcodecs.IMREAD_COLOR)
            val imgCropped = CropBlackEdges.crop(img)

            val roisAutoType = DeviceRoisAutoSelector.getAutoType(img)
            val rois = when (roisAutoType) {
                DeviceRoisAutoSelector.Companion.RoisAutoType.T1 -> DeviceRoisAutoT1(
                    imgCropped.width(), imgCropped.height()
                )

                DeviceRoisAutoSelector.Companion.RoisAutoType.T2 -> DeviceRoisAutoT2(
                    imgCropped.width(), imgCropped.height()
                )
            }
            val extractor = DeviceRoisExtractor(rois, imgCropped)
            val masker = when (roisAutoType) {
                DeviceRoisAutoSelector.Companion.RoisAutoType.T1 -> DeviceRoisMaskerAutoT1()
                DeviceRoisAutoSelector.Companion.RoisAutoType.T2 -> DeviceRoisMaskerAutoT2()
            }
            val knnModel = customKnnModel ?: KNearest.load(ocrDependencyPaths.knnModelFile.path)
            val phashDatabase =
                customPhashDatabase ?: ImagePhashDatabase(ocrDependencyPaths.phashDatabaseFile.path)

            val ocr = DeviceOcr(extractor, masker, knnModel, phashDatabase)
            return ocr.ocr()
        }

        fun ocrResultToScore(
            imageUri: Uri,
            context: Context,
            ocrResult: DeviceOcrResult,
            fallbackDate: Long? = null,
            overrideDate: Long? = null,
            customArcaeaPartnerModifiers: ArcaeaPartnerModifiers? = null,
        ): Score {
            val arcaeaPartnerModifiers =
                customArcaeaPartnerModifiers ?: ArcaeaPartnerModifiers(context.assets)

            val byteArray = IOUtils.toByteArray(context.contentResolver.openInputStream(imageUri))

            val date = if (overrideDate != null) {
                overrideDate
            } else {
                val imgExif = ExifInterface(byteArray.inputStream())
                val imgExifDateTimeOriginal =
                    imgExif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)

                if (imgExifDateTimeOriginal != null) {
                    LocalDateTime.parse(
                        imgExifDateTimeOriginal, DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")
                    ).toInstant(ZoneOffset.UTC).epochSecond
                } else {
                    fallbackDate
                }
            }

            val imgFilename = context.getFilename(imageUri)

            return ocrResult.toScore(
                arcaeaPartnerModifiers = arcaeaPartnerModifiers,
                date = date,
                comment = if (imgFilename != null) "OCR $imgFilename" else null,
            )
        }
    }
}
