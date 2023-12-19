package xyz.sevive.arcaeaoffline.core.ocr.device.rois

import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Rect
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT1
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition.DeviceRoisAutoT2
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.extractor.doubleArrayRoundToRect
import xyz.sevive.arcaeaoffline.core.ocr.device.rois.masker.DeviceRoisMaskerAutoT1

class DeviceRoisAutoSelector {
    companion object {
        private const val pflLabelWidthT1 = 85
        private val pureLabelT1HsvLower = Scalar(80.0, 60.0, 125.0)
        private val pureLabelT1HsvUpper = Scalar(110.0, 200.0, 225.0)

        private const val pflLabelWidthT2 = 180
        private val pureLabelT2HsvLower = Scalar(110.0, 25.0, 90.0)
        private val pureLabelT2HsvUpper = Scalar(160.0, 150.0, 230.0)
        private val farLabelT2HsvLower = Scalar(5.0, 25.0, 120.0)
        private val farLabelT2HsvUpper = Scalar(20.0, 100.0, 240.0)
        private val lostLabelT2HsvLower = Scalar(160.0, 5.0, 190.0)
        private val lostLabelT2HsvUpper = Scalar(179.0, 60.0, 255.0)

        enum class RoisAutoType { T1, T2 }

        private fun pflDoubleArrayToPflLabelRect(array: DoubleArray, labelWidth: Double): Rect {
            return doubleArrayRoundToRect(
                doubleArrayOf(array[0] - labelWidth, array[1], labelWidth, array[3])
            )
        }

        private fun countPflLabelNonZero(roiBgr: Mat, hsvLower: Scalar, hsvUpper: Scalar): Int {
            val roiHsv = Mat()
            Imgproc.cvtColor(roiBgr, roiHsv, Imgproc.COLOR_BGR2HSV)
            val roiMasked = Mat()
            Core.inRange(roiHsv, hsvLower, hsvUpper, roiMasked)
            return Core.countNonZero(roiMasked)
        }

        fun getAutoType(imgBgr: Mat): RoisAutoType {
            val t1Results = mutableListOf<Int>()
            val t2Results = mutableListOf<Int>()

            // AutoT1 ----------
            val roisT1 = DeviceRoisAutoT1(imgBgr.width(), imgBgr.height())
            val maskerT1 = DeviceRoisMaskerAutoT1()
            val pflLabelWidthT1 = pflLabelWidthT1 * roisT1.factor

            val pureLabelRectT1 = pflDoubleArrayToPflLabelRect(roisT1.pure, pflLabelWidthT1)
            t1Results.add(
                countPflLabelNonZero(
                    imgBgr.submat(pureLabelRectT1).clone(),
                    pureLabelT1HsvLower,
                    pureLabelT1HsvUpper,
                )
            )
            listOf(roisT1.far, roisT1.lost).forEach { array ->
                val labelRect = pflDoubleArrayToPflLabelRect(array, pflLabelWidthT1)
                val masked = maskerT1.gray(imgBgr.submat(labelRect).clone())
                t1Results.add(Core.countNonZero(masked))
            }

            // AutoT2 ----------
            val roisT2 = DeviceRoisAutoT2(imgBgr.width(), imgBgr.height())
            val pflLabelWidthT2 = pflLabelWidthT2 * roisT2.factor
            listOf(pureLabelT2HsvLower, farLabelT2HsvLower, lostLabelT2HsvLower).zip(
                listOf(pureLabelT2HsvUpper, farLabelT2HsvUpper, lostLabelT2HsvUpper)
            ).zip(listOf(roisT2.pure, roisT2.far, roisT2.lost)).forEach { (hsvRange, array) ->
                val labelRect = pflDoubleArrayToPflLabelRect(array, pflLabelWidthT2)
                t2Results.add(
                    countPflLabelNonZero(
                        imgBgr.submat(labelRect).clone(),
                        hsvRange.first,
                        hsvRange.second,
                    )
                )
            }

            val results = mutableMapOf(1 to 0, 2 to 0)
            for (i in t1Results.indices) {
                val result = listOf(t1Results[i], t2Results[i])
                val prefer = result.withIndex().maxBy { (_, r) -> r }.index + 1

                results[prefer] = results[prefer]!! + 1
            }

            return when (results.maxBy { (_, v) -> v }.key) {
                1 -> RoisAutoType.T1
                2 -> RoisAutoType.T2

                else -> RoisAutoType.T2
            }
        }
    }
}
