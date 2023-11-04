package xyz.sevive.arcaeaoffline.core.ocr

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.MatOfFloat
import org.opencv.core.MatOfPoint
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc
import org.opencv.ml.KNearest
import org.opencv.objdetect.HOGDescriptor
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

fun resizeFillSquare(img: Mat, target: Int = 20): Mat {
    val h = img.size().height
    val w = img.size().width

    val newSize: Size = if (h > w) {
        Size(w * (target / h), target.toDouble())
    } else {
        Size(target.toDouble(), h * (target / w))
    }
    val resized = Mat()
    Imgproc.resize(img, resized, newSize)

    val borderSize = ceil(
        (max(newSize.width, newSize.height) - min(newSize.width, newSize.height)) / 2
    ).toInt()
    val bordered = Mat()
    if (newSize.width < newSize.height) {
        Core.copyMakeBorder(resized, bordered, 0, 0, borderSize, borderSize, Core.BORDER_CONSTANT)
    } else {
        Core.copyMakeBorder(resized, bordered, borderSize, borderSize, 0, 0, Core.BORDER_CONSTANT)
    }
    val final = Mat()
    Imgproc.resize(bordered, final, Size(target.toDouble(), target.toDouble()))
    return final
}

fun preprocessHog(digitRois: List<Mat>): Mat {
    // https://learnopencv.com/handwritten-digits-classification-an-opencv-c-python-tutorial/
    val samples = mutableListOf<Mat>()
    for (digit in digitRois) {
        val hog = HOGDescriptor(
            Size(20.0, 20.0),
            Size(10.0, 10.0),
            Size(5.0, 5.0),
            Size(10.0, 10.0),
            9,
        )
        val hist = MatOfFloat()
        hog.compute(digit, hist)
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
    return resultStr.toInt(10)
}

fun ocrDigitsByContourGetSamples(roiGray: Mat, size: Int): Mat {
    val roi = roiGray.clone()
    val contours = ArrayList<MatOfPoint>()
    val hierarchy = Mat()
    Imgproc.findContours(roi, contours, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_NONE)
    var rects = contours.map { Imgproc.boundingRect(it) }
    rects = FixRects.connectBroken(rects, roi.width().toDouble(), roi.height().toDouble())
    rects = FixRects.splitConnected(roi, rects)
    val sortedRects = rects.sortedBy { it.x }
    val digitRois = sortedRects.map { resizeFillSquare(roi.submat(it), size) }
    return preprocessHog(digitRois)
}

fun ocrDigitsByContourKnn(
    roiGray: Mat, knnModel: KNearest, k: Int = 4, size: Int = 20
): Int {
    val samples = ocrDigitsByContourGetSamples(roiGray, size)
    return ocrDigitSamplesKnn(samples, knnModel, k)
}
