package xyz.sevive.arcaeaoffline.helpers

import io.github.vinceglb.filekit.PlatformFile
import kotlinx.datetime.UtcOffset
import kotlinx.datetime.asTimeZone
import okio.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import xyz.sevive.arcaeaoffline.TestUtils
import kotlin.time.Instant

// The helper is using androidx dependency, consider migrating to kmp dependency
// and remove RobolectricTestRunner
@RunWith(RobolectricTestRunner::class)
class DeviceOcrHelperTest {
    @Test
    fun `overrideDate has highest priority`() {
        val file = TestUtils.getResourceFile("exif-null.jpg")

        val override = Instant.parse("2026-01-02T03:04:05Z")
        val result =
            DeviceOcrHelper.readImageDateFromExif(
                file,
                overrideDate = override,
            )
        assertEquals(override, result)
    }

    @Test
    fun `returns fallbackDate when EXIF lacks DateTimeOriginal`() {
        val file = TestUtils.getResourceFile("exif-null.jpg")

        val fallback = Instant.parse("2026-01-02T03:04:05Z")
        val result =
            DeviceOcrHelper.readImageDateFromExif(
                file,
                fallbackDate = fallback,
            )
        assertEquals(fallback, result)
    }

    @Test
    fun `returns null when no override, no fallback and no EXIF date`() {
        val file = TestUtils.getResourceFile("exif-null.jpg")

        val result = DeviceOcrHelper.readImageDateFromExif(file)
        assertNull(result)
    }

    @Test
    fun `parses date with timezone offset correctly`() {
        val file = TestUtils.getResourceFile("exif-20750705_0705-tz+0800.jpg")

        val expected = Instant.parse("2075-07-04T23:05:00Z")
        val result =
            DeviceOcrHelper.readImageDateFromExif(
                file,
                // Override default timezone to make sure the offset is correctly parsed and applied
                defaultTimeZoneProvider = { UtcOffset(0, 0).asTimeZone() },
            )
        assertEquals(expected, result)
    }

    @Test
    fun `parses date without offset uses system default timezone`() {
        val file = TestUtils.getResourceFile("exif-20750705_0705-tz_null.jpg")

        val expected = Instant.parse("2075-07-05T06:05:00Z")
        val result =
            DeviceOcrHelper.readImageDateFromExif(
                file,
                defaultTimeZoneProvider = { UtcOffset(1, 0).asTimeZone() },
            )
        assertEquals(expected, result)
    }

    @Test(expected = IOException::class)
    fun `throws on unreadable file`() {
        val file = PlatformFile("haha")

        DeviceOcrHelper.readImageDateFromExif(file)
    }
}
