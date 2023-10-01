package xyz.sevive.arcaeaoffline.ocr.rois.definition

abstract class DeviceAutoRois(w: Int, h: Int): DeviceRois {
    abstract val factor: Double
}

class DeviceAutoRoisT1(val w: Int, val h: Int): DeviceAutoRois(w, h) {
    override val factor: Double
        get() {
            return if (this.w.toDouble() / this.h.toDouble() < 16.0 / 9.0) ((this.w / 16.0) * 9.0) / 720.0 else this.h / 720.0
        }

    val wMid: Double
        get() = this.w / 2.0

    val hMid: Double
        get() = this.h / 2.0

    val topBar: List<Double>
        get() = listOf(0.0, 0.0, this.w.toDouble(), 50.0 * this.factor)

    val layoutAreaHMid: Double
        get() = this.h / 2.0 + this.topBar[3]

    val pflLeftFromWMid: Double
        get() = 5 * this.factor

    val pflX: Double
        get() = this.wMid + this.pflLeftFromWMid

    val pflW: Double
        get() = 76 * this.factor

    val pflH: Double
        get() = 26 * this.factor

    override val pure
        get() = doubleArrayOf(this.pflX, this.layoutAreaHMid + 110.0 * this.factor, this.pflW, this.pflH)

    override val far
        get() = doubleArrayOf(
            this.pflX, this.pure[1] + this.pure[3] + 12.0 * this.factor, this.pflW, this.pflH
        )

    override val lost
        get() = doubleArrayOf(
            this.pflX, this.far[1] + this.far[3] + 10.0 * this.factor, this.pflW, this.pflH
        )
}

class DeviceAutoRoisT2(val w: Int, val h: Int): DeviceAutoRois(w, h) {
    override val factor: Double
        get() {
            return if (this.w.toDouble() / this.h.toDouble() < 16.0 / 9.0) ((this.w / 16.0) * 9.0) / 1080.0 else this.h / 1080.0
        }

    val wMid: Double
        get() = this.w / 2.0

    val hMid: Double
        get() = this.h / 2.0

    val topBar: List<Double>
        get() = listOf(0.0, 0.0, this.w.toDouble(), 75.0 * this.factor)

    val layoutAreaHMid: Double
        get() = this.h / 2.0 + this.topBar[3]

    val pflMidFromWMid: Double
        get() = 60 * this.factor

    val pflX: Double
        get() = this.wMid + 10 * this.factor

    val pflW: Double
        get() = 100 * this.factor

    val pflH: Double
        get() = 24 * this.factor

    override val pure
        get() = doubleArrayOf(this.pflX, this.layoutAreaHMid + 175.0 * this.factor, this.pflW, this.pflH)

    override val far
        get() = doubleArrayOf(
            this.pflX, this.pure[1] + this.pure[3] + 30.0 * this.factor, this.pflW, this.pflH
        )

    override val lost
        get() = doubleArrayOf(
            this.pflX, this.far[1] + this.far[3] + 35.0 * this.factor, this.pflW, this.pflH
        )
}
