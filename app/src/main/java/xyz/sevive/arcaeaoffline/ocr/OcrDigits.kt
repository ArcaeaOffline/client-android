package xyz.sevive.arcaeaoffline.ocr

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.ml.KNearest
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

fun resizeFillSquare(img: Mat, target: Int = 20): Mat {
    val h = img.size().height
    val w = img.size().width

    val newSize: Size = if (h > w) {
        Size(w * (target / h), target.toDouble())
    } else {
        Size(target.toDouble(), h * (target / w))
    }
    val resized = Mat(newSize, img.type())
    Imgproc.resize(img, resized, newSize)

    val borderSize = ceil(
        max(newSize.width, newSize.height) - min(newSize.width, newSize.height) / 2
    ).roundToInt()
    val borderedSize: Size
    val bordered: Mat
    if (newSize.width < newSize.height) {
        borderedSize = Size(newSize.width + borderSize * 2, newSize.height)
        bordered = Mat(borderedSize, img.type())
        Core.copyMakeBorder(resized, bordered, 0, 0, borderSize, borderSize, Core.BORDER_CONSTANT)
    } else {
        borderedSize = Size(newSize.width, newSize.height + borderSize * 2)
        bordered = Mat(borderedSize, img.type())
        Core.copyMakeBorder(resized, bordered, borderSize, borderSize, 0, 0, Core.BORDER_CONSTANT)
    }
    val final = Mat(target, target, img.type())
    Imgproc.resize(bordered, final, Size(target.toDouble(), target.toDouble()))
    return final
}

fun preprocessHog(digitRois: List<Mat>): Mat {
    // https://github.com/opencv/opencv/blob/f834736307c8328340aea48908484052170c9224/samples/python/digits.py
    val samples = mutableListOf<Mat>()
    for (digit in digitRois) {
        val gx = Mat()
        val gy = Mat()
        Imgproc.Sobel(digit, gx, CvType.CV_32F, 1, 0)
        Imgproc.Sobel(digit, gy, CvType.CV_32F, 0, 1)
        val mag = Mat()
        val ang = Mat()
        Core.cartToPolar(gx, gy, mag, ang)
        val binN = 16
        val _bin = Mat()
        ang.convertTo(_bin, CvType.CV_32S, binN / (2 * PI))
        val bin_cells = arrayOf(
            _bin.submat(0, 10, 0, 10),
            _bin.submat(10, 20, 0, 10),
            _bin.submat(0, 10, 10, 20),
            _bin.submat(10, 20, 10, 20)
        )
        val mag_cells = arrayOf(
            mag.submat(0, 10, 0, 10),
            mag.submat(10, 20, 0, 10),
            mag.submat(0, 10, 10, 20),
            mag.submat(10, 20, 10, 20)
        )
        val hists = mutableListOf<Mat>()
        for ((b, m) in bin_cells.zip(mag_cells)) {
            val hist = Mat.ones(binN, 1, CvType.CV_32F)
            for (i in 0 until b.rows()) {
                for (j in 0 until b.cols()) {
                    val bin = b.get(i, j)[0].toInt()
                    val mag = m.get(i, j)[0]
                    val iDontKnowWhatIsThis = hist.get(bin, 0)
                    if (iDontKnowWhatIsThis != null) {
                        hist.put(bin, 0, hist.get(bin, 0)[0] + mag)
                    } else {
                        hist.put(bin, 0, 0.0)
                    }
                }
            }
            hists.add(hist)
        }
        val hist = Mat()
        Core.vconcat(hists, hist)

        // transform to Hellinger kernel
        val eps = 1e-7
        Core.divide(hist, Scalar(Core.sumElems(hist).`val`[0] + eps), hist)
        Core.sqrt(hist, hist)
        Core.normalize(hist, hist, 1.0, 0.0, Core.NORM_L2)

        samples.add(hist)
    }
    val mat = Mat()
    Core.hconcat(samples.reversed(), mat)
    Core.rotate(mat, mat, Core.ROTATE_90_COUNTERCLOCKWISE)
    return mat
}

fun ocrDigitSamplesKnn(samples: Mat, knnModel: KNearest, k: Int = 4): Int {
    val results = Mat()
    knnModel.findNearest(samples, k, results)
    var resultStr = ""
    for (row in 0 until results.rows()) {
        val data = results.get(row, 0)
        if (data == null || data[0] < 0.0) continue
        resultStr += data[0].toInt().toString()
    }
    return resultStr.toInt()
}

fun ocrDigitsByContourGetSamples(roiGray: Mat, size: Int): Mat {
    val roi = roiGray.clone()
    val contours = ArrayList<MatOfPoint>()
    val hierarchy = Mat()
    Imgproc.findContours(roi, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE)
    val rects = contours.map { Imgproc.boundingRect(it) }
//    val fixedRects = FixRects.connectBroken(rects, roi.cols(), roi.rows())
//    val splitRects = FixRects.splitConnected(roi, fixedRects)
    val sortedRects = rects.sortedBy { it.x }
    // val digitRois = sortedRects.map { resize(cropXywh(roi, it), Size(size.toDouble(), size.toDouble())) }
    val digitRois = sortedRects.map { resizeFillSquare(roi.submat(it), size) }
    return preprocessHog(digitRois)
}

fun ocrDigitsByContourKnn(
    roiGray: Mat,
    knnModel: KNearest,
    k: Int = 4,
    size: Int = 20
): Int {
    val samples = ocrDigitsByContourGetSamples(roiGray, size)
    return ocrDigitSamplesKnn(samples, knnModel, k)
}