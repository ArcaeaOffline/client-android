package xyz.sevive.arcaeaoffline.ocr

import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import org.opencv.ml.KNearest
import xyz.sevive.arcaeaoffline.ocr.rois.DeviceRoisExtractor
import xyz.sevive.arcaeaoffline.ocr.rois.DeviceRoisMasker

data class DeviceOcrResult(
    val ratingClass: Int,
    val pure: Int,
    val far: Int,
    val lost: Int,
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

    fun pfl(roiGray: Mat, factor: Double = 1.25): Int {
        val contours = ArrayList<MatOfPoint>()
        Imgproc.findContours(
            roiGray,
            contours,
            Mat(),
            Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_NONE
        )
        val filteredContours = contours.filter { Imgproc.contourArea(it) >= 5 * factor }
        var rects = filteredContours.map { Imgproc.boundingRect(it) }
        rects =
            FixRect.connectBroken(rects, roiGray.width().toDouble(), roiGray.height().toDouble())

        var filteredRects = rects.filter { it.width >= 5 * factor && it.height >= 6 * factor }
        filteredRects = FixRect.splitConnected(roiGray, filteredRects)
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
}