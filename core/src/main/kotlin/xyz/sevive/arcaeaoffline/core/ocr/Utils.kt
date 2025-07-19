package xyz.sevive.arcaeaoffline.core.ocr

import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sqrt

fun matMedian(mat: Mat): Double {
    // Convert the matrix to a single row vector
    val arr = mat.clone().reshape(1, mat.rows() * mat.cols())

    val arrSorted = Mat()
    Core.sort(arr, arrSorted, Core.SORT_EVERY_COLUMN + Core.SORT_ASCENDING)

    val rows = arr.rows()
    return if (rows % 2 == 0) {
        val midIndex1 = rows / 2
        val midIndex2 = midIndex1 - 1

        val midValue1 = arrSorted.get(midIndex1, 0)[0]
        val midValue2 = arrSorted.get(midIndex2, 0)[0]
        (midValue1 + midValue2) / 2.0
    } else {
        val middleIndex = rows / 2
        arrSorted.get(middleIndex, 0)[0]
    }
}


/**
 * Port of numpy.bincount.
 *
 * [Original documentation](https://numpy.org/doc/stable/reference/generated/numpy.bincount.html#numpy-bincount)
 *
 * @param x array_like, 1 dimension, nonnegative ints.
 * @param weights array_like, same shape as x.
 * @param minLength int, minimum number of bins for the output array.
 */
@Suppress("unused")
fun binCount(x: Mat, weights: Mat? = null, minLength: Int = 0): Mat {
    assert(x.cols() == 1) { "binCount: `x` should be a one dimension array" }
    if (weights != null) {
        assert(weights.size() == x.size()) { "binCount: `weights` should have the same size as `x`" }
    }
    val checkMat = Mat()
    Core.compare(x, Scalar(0.0), checkMat, Core.CMP_GE)
    assert(Core.countNonZero(checkMat) == x.rows()) { "binCount: `x` should not have negative values" }

    // determine bin size
    val binSizeFromMat = Core.minMaxLoc(x).maxVal.toInt() + 1
    val binSize = if (minLength > binSizeFromMat) minLength else binSizeFromMat

    // extract values
    val matValues = mutableListOf<Int>()
    for (i in 0 until x.rows()) {
        matValues.add(x.get(i, 0)[0].toInt())
    }

    val binArr = (0 until binSize).map { index -> matValues.count { value -> index == value } }
    var binResultArr = binArr.map { it.toDouble() }

    if (weights != null) {
        val weightValues = mutableListOf<Double>()
        for (i in 0 until weights.rows()) {
            weightValues.add(weights.get(i, 0)[0])
        }
        val binWeights = (0 until binSize).map { 0.0 }.toMutableList()
        for ((index, weight) in matValues.zip(weightValues)) {
            binWeights[index] = binWeights[index] + weight
        }
        binResultArr = binResultArr.zip(binWeights).map { (value, weight) -> value * weight }
    }

    val resultMat = Mat.zeros(binSize, 1, CvType.CV_32F)
    for (i in 0 until binSize) {
        resultMat.put(i, 0, binResultArr[i])
    }
    return resultMat
}

fun collectionStandardDeviation(list: Collection<Double>): Double {
    // https://www.programmingcube.com/write-a-kotlin-program-to-calculate-standard-deviation
    val mean = list.average()

    val squaredDifferences = list.map { (it - mean) * (it - mean) }
    val meanOfSquaredDifferences = squaredDifferences.average()

    return sqrt(meanOfSquaredDifferences)
}

fun collectionMedian(list: List<Double>) = list.sorted().let {
    if (it.size % 2 == 0) (it[it.size / 2] + it[(it.size - 1) / 2]) / 2
    else it[it.size / 2]
}

class FixRects {
    companion object {
        fun connectBroken(
            rects: List<Rect>, imgWidth: Double, imgHeight: Double, overrideTolerance: Int? = null
        ): List<Rect> {
            val tolerance: Int = overrideTolerance ?: floor(imgWidth * 0.08).toInt()

            val newRects = mutableListOf<Rect>()
            val consumedRects = mutableListOf<Rect>()

            for (rect in rects) {
                if (consumedRects.indexOf(rect) > -1) continue

                // filter out large rects
                if (!(imgHeight * 0.1 <= rect.height && rect.height <= imgHeight * 0.6)) continue


                val group = mutableListOf<Rect>()
                // see if there's other rects that have near left & right borders
                for (otherRect in rects) {
                    if (rect == otherRect) continue

                    if (abs(rect.x - otherRect.x) < tolerance && abs((rect.x + rect.width) - (otherRect.x + otherRect.width)) < tolerance) {
                        group.add(otherRect)
                    }
                }

                if (group.size > 0) {
                    group.add(rect)
                    consumedRects.addAll(group)
                    // calculate new rect
                    val newX = group.minBy { it.x }.x
                    val newY = group.minBy { it.y }.y
                    val newRightRect = group.maxBy { it.x + it.width }
                    val newRight = newRightRect.x + newRightRect.width
                    val newBottomRect = group.maxBy { it.y + it.height }
                    val newBottom = newBottomRect.y + newBottomRect.height
                    val newW = newRight - newX
                    val newH = newBottom - newY
                    newRects.add(Rect(newX, newY, newW, newH))
                }
            }

            val returnRects = rects.filter { consumedRects.indexOf(it) == -1 }.toMutableList()
            returnRects.addAll(newRects)
            return returnRects.toList()
        }

        fun splitConnected(
            imgMasked: Mat,
            rects: List<Rect>,
            rectWHRatio: Double = 1.05,
            widthRangeRatio: Double = 0.1
        ): List<Rect> {
            val connectedRects = mutableListOf<Rect>()
            val newRects = mutableListOf<Rect>()

            for (rect in rects) {
                if ((rect.width.toDouble() / rect.height.toDouble()) <= rectWHRatio) continue

                connectedRects.add(rect)

                // find the thinnest part
                val borderIgnore = round(rect.width * widthRangeRatio).toInt()

                val imgCropped = imgMasked.submat(
                    Rect(
                        borderIgnore, rect.y, rect.width - borderIgnore, rect.height
                    )
                ).clone()
                val whitePixels = mutableMapOf<Int, Int>()
                for (i in 0 until imgCropped.rows()) {
                    val col = imgCropped.submat(i, i + 1, 0, imgCropped.cols()).clone()
                    whitePixels[rect.x + borderIgnore + i] = Core.countNonZero(col)
                }

                if (whitePixels.values.all { it == 0 }) return rects

                val leastWhitePixels = whitePixels.values.minBy { it }
                val xValuesMap = whitePixels.filter { it.value == leastWhitePixels }
                val xValues = xValuesMap.keys.map { it.toDouble() }

                // select only middle values
                val xMean = xValues.average()
                val xStd = collectionStandardDeviation(xValues)
                val xValuesInRange = xValues.filter {
                    xMean - xStd * 1.5 <= it && it <= xMean + xStd * 1.5
                }
                val xMid = collectionMedian(xValuesInRange).roundToInt()

                // split rect
                newRects.add(Rect(rect.x, rect.y, xMid - rect.x, rect.height))
                newRects.add(Rect(xMid, rect.y, rect.x + rect.width - xMid, rect.height))
            }

            val returnRects = rects.filter { connectedRects.indexOf(it) == -1 }.toMutableList()
            returnRects.addAll(newRects)
            return returnRects.toList()
        }
    }
}
