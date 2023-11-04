package xyz.sevive.arcaeaoffline.core.ocr

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.core.ocr.rois.extractor.DeviceRoisExtractor
import xyz.sevive.arcaeaoffline.core.ocr.rois.masker.DeviceRoisMasker

data class DeviceOcrResult(
    val ratingClass: Int,
    val pure: Int,
    val far: Int,
    val lost: Int,
    val score: Int,
    val maxRecall: Int?,
    val songId: String?,
    val songIdPossibility: Double?,
    val clearStatus: Int?,
    val partnerId: String?,
    val partnerIdPossibility: Double?,
)

class DeviceOcr(
    val extractor: DeviceRoisExtractor,
    val masker: DeviceRoisMasker,
    val knnModel: KNearest,
    val phashDb: ImagePhashDatabase
) {
    companion object {
        fun preprocessPartnerIcon(imgGray: Mat): Mat {
            val iconSquared = Mat()
            Core.copyMakeBorder(
                imgGray,
                iconSquared,
                imgGray.width() - imgGray.height(),
                0,
                0,
                0,
                Core.BORDER_REPLICATE
            )
            val w = iconSquared.width().toDouble()
            val h = iconSquared.height().toDouble()
            Imgproc.fillPoly(
                iconSquared,
                listOf(
                    MatOfPoint(Point(0.0, 0.0), Point(w / 2, 0.0), Point(0.0, h / 2)),
                    MatOfPoint(Point(w, 0.0), Point(w / 2, 0.0), Point(w, h / 2)),
                    MatOfPoint(Point(0.0, h), Point(w / 2, h), Point(0.0, h / 2)),
                    MatOfPoint(Point(w, h), Point(w / 2, h), Point(w, h / 2))
                ),
                Scalar(128.0)
            )
            return iconSquared
        }
    }

    fun pfl(roiGray: Mat, factor: Double = 1.0): Int {
        val contours = ArrayList<MatOfPoint>()
        Imgproc.findContours(
            roiGray, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE
        )
        val filteredContours = contours.filter { Imgproc.contourArea(it) >= 5 * factor }
        var rects = filteredContours.map { Imgproc.boundingRect(it) }
        rects =
            FixRects.connectBroken(rects, roiGray.width().toDouble(), roiGray.height().toDouble())

        var filteredRects = rects.filter { it.width >= 5 * factor && it.height >= 6 * factor }
        filteredRects = FixRects.splitConnected(roiGray, filteredRects)
        filteredRects = filteredRects.sortedBy { it.x }

        val roiOcr = roiGray.clone()
        for (contour in contours) {
            if (filteredContours.indexOf(contour) > -1) continue
            Imgproc.fillPoly(roiOcr, listOf(contour), Scalar(0.0))
        }

        val digitRois =
            filteredRects.map { rect -> resizeFillSquare(roiOcr.submat(rect).clone(), 20) }
        val samples = preprocessHog(digitRois)
        return ocrDigitSamplesKnn(samples, this.knnModel)
    }

    fun pure() = pfl(masker.pure(extractor.pure))
    fun far() = pfl(masker.far(extractor.far))
    fun lost() = pfl(masker.lost(extractor.lost))

    fun score(): Int {
        val roi = masker.score(extractor.score)
        val contours = ArrayList<MatOfPoint>()
        Imgproc.findContours(
            roi, contours, Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE
        )
        for (contour in contours) {
            if (Imgproc.boundingRect(contour).height < roi.height() * 0.6) {
                Imgproc.fillPoly(roi, listOf(contour), Scalar(0.0))
            }
        }
        return ocrDigitsByContourKnn(roi, knnModel)
    }

    fun ratingClass(): Int {
        val roi = extractor.ratingClass
        val results = listOf<Mat>(
            masker.ratingClassPst(roi),
            masker.ratingClassPrs(roi),
            masker.ratingClassFtr(roi),
            masker.ratingClassByd(roi),
        )
        return results.indices.maxBy { Core.countNonZero(results[it]) }
    }

    fun maxRecall(): Int = ocrDigitsByContourKnn(masker.maxRecall(extractor.maxRecall), knnModel)

    fun clearStatus(): Int {
        val roi = extractor.clearStatus
        val results = listOf<Mat>(
            masker.clearStatusTrackLost(roi),
            masker.clearStatusTrackComplete(roi),
            masker.clearStatusFullRecall(roi),
            masker.clearStatusPureMemory(roi),
        )
        return results.indices.maxBy { Core.countNonZero(results[it]) }
    }

    fun lookupSongId(): Pair<String, Int> {
        val roiGray = Mat()
        Imgproc.cvtColor(extractor.jacket, roiGray, Imgproc.COLOR_BGR2GRAY)
        return phashDb.lookupJacket(roiGray)
    }

    fun songId(): String = lookupSongId().first

    fun lookupPartnerId(): Pair<String, Int> {
        val roiGray = Mat()
        Imgproc.cvtColor(extractor.partnerIcon, roiGray, Imgproc.COLOR_BGR2GRAY)
        return phashDb.lookupPartnerIcon(preprocessPartnerIcon(roiGray))
    }

    fun partnerId(): String = lookupPartnerId().first

    fun ocr(): DeviceOcrResult {
        val phashLen = phashDb.hashSize * phashDb.hashSize
        val (songId, songIdDistance) = lookupSongId()
        val (partnerId, partnerIdDistance) = lookupPartnerId()

        return DeviceOcrResult(
            ratingClass = ratingClass(),
            pure = pure(),
            far = far(),
            lost = lost(),
            score = score(),
            maxRecall = maxRecall(),
            songId = songId,
            songIdPossibility = 1 - songIdDistance / phashLen.toDouble(),
            clearStatus = clearStatus(),
            partnerId = partnerId,
            partnerIdPossibility = 1 - partnerIdDistance / phashLen.toDouble(),
        )
    }
}
