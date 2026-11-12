package xyz.sevive.arcaeaoffline.core.ocr.layouts

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceLayoutTest {
    companion object {
        private const val DELTA = 1e-6
    }

    @Test
    fun builtinLayoutsParseWithAllFields() {
        for (name in listOf("t1", "t2")) {
            val layout = DeviceLayout.loadBuiltin(name)
            assertEquals(listOf(1280, 720), layout.design)
            assertEquals(15, layout.fields.size)
        }
    }

    @Test
    fun screenRectOnCanvasAspectRatio() {
        // 1920x1080 is the design canvas scaled by 1.5
        val layout = DeviceLayout.loadBuiltin("t1")
        assertArrayEquals(
            doubleArrayOf(967.5, 780.0, 114.0, 39.0),
            layout.screenRect("pure", 1920, 1080),
            DELTA,
        )
    }

    @Test
    fun screenRectOnNarrowerScreenCentersVertically() {
        // rating_class has a negative design x, so the mapped x clamps to 0
        val layout = DeviceLayout.loadBuiltin("t2")
        assertArrayEquals(
            doubleArrayOf(0.0, 248.0, 186.666667, 58.666667),
            layout.screenRect("rating_class", 1024, 768),
            DELTA,
        )
    }

    @Test
    fun screenRectOnWiderScreenScalesByHeight() {
        // wider than the canvas aspect, so the canvas scales by height (1.5) and centers
        // horizontally
        val layout = DeviceLayout.loadBuiltin("t1")
        assertArrayEquals(
            doubleArrayOf(1287.5, 780.0, 114.0, 39.0),
            layout.screenRect("pure", 2560, 1080),
            DELTA,
        )
    }
}
