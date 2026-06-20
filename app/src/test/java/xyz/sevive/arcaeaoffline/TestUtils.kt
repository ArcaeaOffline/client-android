package xyz.sevive.arcaeaoffline

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.projectDir

class TestUtils {
    companion object {
        val resourceDirectory = FileKit.projectDir / "src/test/resources"

        fun getResourceFile(filename: String): PlatformFile = PlatformFile(resourceDirectory, filename)
    }
}
