package xyz.sevive.arcaeaoffline.core.ocr.device

import ai.onnxruntime.OrtSession
import kotlinx.serialization.Serializable
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import xyz.sevive.arcaeaoffline.core.ArcaeaPartnerModifiers
import xyz.sevive.arcaeaoffline.core.clearStatusToClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.PlayResult
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashItem
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashesDatabase
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.extractor.DeviceRoisExtractor
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceRoisMasker
import xyz.sevive.arcaeaoffline.core.ocr.getMostConfidentItem
import kotlin.time.Instant
import kotlin.uuid.Uuid

@Serializable
data class DeviceOcrResult(
    val ratingClass: ArcaeaRatingClass,
    val pure: Int,
    val far: Int,
    val lost: Int,
    val score: Int,
    val maxRecall: Int?,
    val songIdResults: List<ImageHashItem>,
    val clearStatus: Int?,
    val partnerIdResults: List<ImageHashItem>,
) {
    val songId =
        if (songIdResults.isEmpty()) {
            ""
        } else {
            songIdResults.getMostConfidentItem()?.label ?: ""
        }

    val partnerId =
        if (partnerIdResults.isEmpty()) {
            ""
        } else {
            partnerIdResults.getMostConfidentItem()?.label ?: ""
        }
}

fun DeviceOcrResult.toPlayResult(
    date: Instant? = null,
    comment: String? = null,
): PlayResult {
    val playResultModifier = ArcaeaPartnerModifiers[this.partnerId]
    val clearType = this.clearStatus?.let { clearStatusToClearType(it, playResultModifier) }

    return PlayResult(
        id = 0,
        uuid = Uuid.generateV4(),
        songId = this.songId,
        ratingClass = this.ratingClass,
        score = this.score,
        pure = this.pure,
        far = this.far,
        lost = this.lost,
        date = date,
        maxRecall = this.maxRecall,
        modifier = playResultModifier,
        clearType = clearType,
        comment = comment,
    )
}

class DeviceOcr(
    private val extractor: DeviceRoisExtractor,
    private val masker: DeviceRoisMasker,
    private val ortSession: OrtSession,
    private val hashesDb: ImageHashesDatabase,
) {
    companion object {
        fun preprocessPartnerIcon(imgGray: Mat): Mat {
            val iconSquared = Mat()
            if (imgGray.width() > imgGray.height()) {
                Core.copyMakeBorder(
                    imgGray,
                    iconSquared,
                    imgGray.width() - imgGray.height(),
                    0,
                    0,
                    0,
                    Core.BORDER_REPLICATE,
                )
            } else {
                imgGray.copyTo(iconSquared)
            }

            val w = iconSquared.width().toDouble()
            val h = iconSquared.height().toDouble()
            Imgproc.fillPoly(
                iconSquared,
                listOf(
                    MatOfPoint(Point(0.0, 0.0), Point(w / 2, 0.0), Point(0.0, h / 2)),
                    MatOfPoint(Point(w, 0.0), Point(w / 2, 0.0), Point(w, h / 2)),
                    MatOfPoint(Point(0.0, h), Point(w / 2, h), Point(0.0, h / 2)),
                    MatOfPoint(Point(w, h), Point(w / 2, h), Point(w, h / 2)),
                ),
                Scalar(128.0),
            )
            return iconSquared
        }
    }

    fun ratingClass(): ArcaeaRatingClass {
        val roi = extractor.ratingClass
        val results =
            listOf(
                masker.ratingClassPst(roi),
                masker.ratingClassPrs(roi),
                masker.ratingClassFtr(roi),
                masker.ratingClassByd(roi),
                masker.ratingClassEtr(roi),
            )
        return ArcaeaRatingClass.fromInt(results.indices.maxBy { Core.countNonZero(results[it]) })
    }

    private fun clearStatus(): Int {
        val roi = extractor.clearStatus
        val results =
            listOf(
                masker.clearStatusTrackLost(roi),
                masker.clearStatusTrackComplete(roi),
                masker.clearStatusFullRecall(roi),
                masker.clearStatusPureMemory(roi),
            )
        return results.indices.maxBy { Core.countNonZero(results[it]) }
    }

    private fun lookupSongId(): List<ImageHashItem> {
        val roiGray = Mat()
        Imgproc.cvtColor(extractor.jacket, roiGray, Imgproc.COLOR_BGR2GRAY)
        return hashesDb.lookupJacket(roiGray)
    }

    private fun lookupPartnerId(): List<ImageHashItem> {
        val roiGray = Mat()
        Imgproc.cvtColor(extractor.partnerIcon, roiGray, Imgproc.COLOR_BGR2GRAY)
        return hashesDb.lookupPartnerIcon(preprocessPartnerIcon(roiGray))
    }

    fun ocr(): DeviceOcrResult =
        DeviceOcrResult(
            ratingClass = ratingClass(),
            pure = DeviceOcrOnnxHelper.ocrBgrMat(extractor.pure, ortSession).toInt(),
            far = DeviceOcrOnnxHelper.ocrBgrMat(extractor.far, ortSession).toInt(),
            lost = DeviceOcrOnnxHelper.ocrBgrMat(extractor.lost, ortSession).toInt(),
            score = DeviceOcrOnnxHelper.ocrBgrMat(extractor.score, ortSession).toInt(),
            maxRecall = DeviceOcrOnnxHelper.ocrBgrMat(extractor.maxRecall, ortSession).toInt(),
            songIdResults = lookupSongId(),
            clearStatus = clearStatus(),
            partnerIdResults = lookupPartnerId(),
        )
}
