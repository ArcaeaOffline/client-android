package xyz.sevive.arcaeaoffline.core.ocr.device.rois.definition

import xyz.sevive.arcaeaoffline.core.ocr.layouts.DeviceLayout

abstract class DeviceRoisAuto(
    open val w: Int,
    open val h: Int,
) : DeviceRois {
    abstract val factor: Double
}

/**
 * Rois whose field windows come from a [DeviceLayout] mapped onto the screenshot.
 *
 * The one exception is [partnerIcon]: it pins to the physical screen top and has no
 * window on the layout canvas.
 */
abstract class DeviceRoisFromLayout(
    w: Int,
    h: Int,
) : DeviceRoisAuto(w, h) {
    protected abstract val layout: DeviceLayout

    override val pure: DoubleArray
        get() = layout.screenRect("pure", w, h)

    override val far: DoubleArray
        get() = layout.screenRect("far", w, h)

    override val lost: DoubleArray
        get() = layout.screenRect("lost", w, h)

    override val score: DoubleArray
        get() = layout.screenRect("score", w, h)

    override val ratingClass: DoubleArray
        get() = layout.screenRect("rating_class", w, h)

    override val maxRecall: DoubleArray
        get() = layout.screenRect("max_recall", w, h)

    override val jacket: DoubleArray
        get() = layout.screenRect("jacket", w, h)

    override val clearStatus: DoubleArray
        get() = layout.screenRect("clear_status", w, h)

    override val highScore: DoubleArray
        get() = layout.screenRect("high_score", w, h)

    override val highScoreDelta: DoubleArray
        get() = layout.screenRect("high_score_delta", w, h)

    override val pureShiny: DoubleArray
        get() = layout.screenRect("pure_shiny", w, h)

    override val earlyLate: DoubleArray
        get() = layout.screenRect("early_late", w, h)

    override val pureLabel: DoubleArray
        get() = layout.screenRect("pure_label", w, h)

    override val farLabel: DoubleArray
        get() = layout.screenRect("far_label", w, h)

    override val lostLabel: DoubleArray
        get() = layout.screenRect("lost_label", w, h)
}

class DeviceRoisAutoT1(
    override val w: Int,
    override val h: Int,
) : DeviceRoisFromLayout(w, h) {
    companion object {
        private val LAYOUT = DeviceLayout.loadBuiltin("t1")
    }

    override val layout: DeviceLayout
        get() = LAYOUT

    override val partnerIcon: DoubleArray
        get() {
            val w = 90.0 * factor
            val h = 75.0 * factor
            return doubleArrayOf(wMid - w / 2, 0.0, w, h)
        }

    override val factor: Double
        get() = if (w.toDouble() / h < 16.0 / 9.0) (w / 16.0) * 9.0 / 720.0 else h / 720.0

    private val wMid: Double
        get() = w / 2.0
}

class DeviceRoisAutoT2(
    override val w: Int,
    override val h: Int,
) : DeviceRoisFromLayout(w, h) {
    companion object {
        private val LAYOUT = DeviceLayout.loadBuiltin("t2")
    }

    override val layout: DeviceLayout
        get() = LAYOUT

    override val partnerIcon: DoubleArray
        get() {
            val w = 135.0 * factor
            val h = 110.0 * factor
            return doubleArrayOf(wMid - w / 2, 0.0, w, h)
        }

    // screen-anchored helpers: ScreenshotDetect positions its probe against them
    override val factor: Double
        get() = if (w.toDouble() / h < 16.0 / 9.0) (w / 16.0) * 9.0 / 1080.0 else h / 1080.0

    val wMid: Double
        get() = w / 2.0

    val layoutAreaHMid: Double
        get() = h / 2.0 + 75.0 * factor
}
