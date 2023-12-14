package xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition

import kotlin.math.max

abstract class DeviceAutoRois(open val w: Int, open val h: Int) : DeviceRois {
    abstract val factor: Double
}

class DeviceAutoRoisT1(override val w: Int, override val h: Int) : DeviceAutoRois(w, h) {
    override val factor: Double
        get() {
            return if (this.w.toDouble() / this.h.toDouble() < 16.0 / 9.0) ((this.w / 16.0) * 9.0) / 720.0 else this.h / 720.0
        }

    private val wMid: Double
        get() = this.w / 2.0

//    val hMid: Double
//        get() = this.h / 2.0

    private val topBar: List<Double>
        get() = listOf(0.0, 0.0, this.w.toDouble(), 50.0 * this.factor)

    private val layoutAreaHMid: Double
        get() = this.h / 2.0 + this.topBar[3]

    private val pflLeftFromWMid: Double
        get() = 5 * this.factor

    private val pflX: Double
        get() = this.wMid + this.pflLeftFromWMid

    private val pflW: Double
        get() = 76 * this.factor

    private val pflH: Double
        get() = 26 * this.factor

    override val pure
        get() = doubleArrayOf(
            this.pflX, this.layoutAreaHMid + 110.0 * this.factor, this.pflW, this.pflH
        )

    override val far
        get() = doubleArrayOf(
            this.pflX, this.pure[1] + this.pure[3] + 12.0 * this.factor, this.pflW, this.pflH
        )

    override val lost
        get() = doubleArrayOf(
            this.pflX, this.far[1] + this.far[3] + 10.0 * this.factor, this.pflW, this.pflH
        )

    override val score: DoubleArray
        get() {
            val w = 280 * this.factor
            val h = 45 * this.factor
            return doubleArrayOf(
                this.wMid - w / 2, this.layoutAreaHMid - 75 * this.factor - h, w, h
            )
        }

    override val ratingClass: DoubleArray
        get() = doubleArrayOf(
            this.wMid - 610 * this.factor,
            this.layoutAreaHMid - 180 * this.factor,
            265 * this.factor,
            35 * this.factor,
        )

    override val maxRecall: DoubleArray
        get() = doubleArrayOf(
            this.wMid - 465 * this.factor,
            this.layoutAreaHMid - 215 * this.factor,
            150 * this.factor,
            35 * this.factor,
        )

    override val jacket: DoubleArray
        get() = doubleArrayOf(
            this.wMid - 610 * this.factor,
            this.layoutAreaHMid - 143 * this.factor,
            375 * this.factor,
            375 * this.factor,
        )

    override val clearStatus: DoubleArray
        get() {
            val w = 550 * this.factor
            val h = 60 * this.factor
            return doubleArrayOf(
                this.wMid - w / 2,
                this.layoutAreaHMid - 155 * this.factor - h,
                w,
                h,
            )
        }

    override val partnerIcon: DoubleArray
        get() {
            val w = 90 * this.factor
            val h = 75 * this.factor
            return doubleArrayOf(this.wMid - w / 2, 0.0, w, h)
        }
}

class DeviceAutoRoisT2(override val w: Int, override val h: Int) : DeviceAutoRois(w, h) {
    override val factor: Double
        get() {
            return if (this.w.toDouble() / this.h.toDouble() < 16.0 / 9.0) ((this.w / 16.0) * 9.0) / 1080.0 else this.h / 1080.0
        }

    val wMid: Double
        get() = this.w / 2.0

//    val hMid: Double
//        get() = this.h / 2.0

    private val topBar: List<Double>
        get() = listOf(0.0, 0.0, this.w.toDouble(), 75.0 * this.factor)

    val layoutAreaHMid: Double
        get() = this.h / 2.0 + this.topBar[3]

//    val pflMidFromWMid: Double
//        get() = 60 * this.factor

    private val pflX: Double
        get() = this.wMid + 10 * this.factor

    private val pflW: Double
        get() = 100 * this.factor

    private val pflH: Double
        get() = 24 * this.factor

    override val pure: DoubleArray
        get() = doubleArrayOf(
            this.pflX, this.layoutAreaHMid + 175.0 * this.factor, this.pflW, this.pflH
        )

    override val far: DoubleArray
        get() = doubleArrayOf(
            this.pflX, this.pure[1] + this.pure[3] + 30.0 * this.factor, this.pflW, this.pflH
        )

    override val lost: DoubleArray
        get() = doubleArrayOf(
            this.pflX, this.far[1] + this.far[3] + 35.0 * this.factor, this.pflW, this.pflH
        )

    override val score: DoubleArray
        get() {
            val w = 420.0 * this.factor
            val h = 70.0 * this.factor
            return doubleArrayOf(
                this.wMid - w / 2.0, this.layoutAreaHMid - 110 * this.factor - h, w, h
            )
        }

    override val ratingClass: DoubleArray
        get() = doubleArrayOf(
            max(0.0, this.wMid - 965 * this.factor),
            this.layoutAreaHMid - 330 * this.factor,
            350 * this.factor,
            110 * this.factor,
        )

    override val maxRecall: DoubleArray
        get() = doubleArrayOf(
            this.wMid - 625 * this.factor,
            this.layoutAreaHMid - 275 * this.factor,
            150 * this.factor,
            50 * this.factor,
        )

    override val jacket: DoubleArray
        get() = doubleArrayOf(
            this.wMid - 915 * this.factor,
            this.layoutAreaHMid - 215 * this.factor,
            565 * this.factor,
            565 * this.factor,
        )

    override val clearStatus: DoubleArray
        get() {
            val w = 825 * this.factor
            val h = 90 * this.factor
            return doubleArrayOf(
                this.wMid - w / 2,
                this.layoutAreaHMid - 235 * this.factor - h,
                w,
                h,
            )
        }

    override val partnerIcon: DoubleArray
        get() {
            val w = 135 * this.factor
            val h = 110 * this.factor
            return doubleArrayOf(this.wMid - w / 2, 0.0, w, h)
        }
}
