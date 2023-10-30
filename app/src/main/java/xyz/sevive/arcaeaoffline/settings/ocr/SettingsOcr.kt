package xyz.sevive.arcaeaoffline.settings


import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File

class SettingsOcr(val context: Context) {
    val ocrFilesParent: File
        get() {
            return File(context.filesDir, "ocr/dependencies")
        }

    fun knnModelFile() = File(this.ocrFilesParent.path, "digits.knn.dat")
    fun pHashDatabaseFile() = File(this.ocrFilesParent.path, "image-phash.db")

    val knnModelOk: Boolean
        get() {
            return knnModelFile().exists()
        }

    fun ocrFilesParentMkdirs() {
        if (!ocrFilesParent.exists()) {
            if (!ocrFilesParent.mkdirs()) {
                Log.w("SettingsOCR", "ocrFilesParent mkdirs failed, $ocrFilesParent")
            }
        }
    }

    private fun importFile(src: Uri, dst: File) {
        val resolver = this.context.contentResolver
        dst.delete()

        val outputStream = dst.outputStream()
        resolver.openInputStream(src)?.copyTo(outputStream)
    }

    fun importKnn(fileUri: Uri) {
        val builtinKnnModelFile = knnModelFile()

        ocrFilesParentMkdirs()
        importFile(fileUri, builtinKnnModelFile)
    }

    fun importPHashDatabase(fileUri: Uri) {
        val builtinPHashDatabaseFile = pHashDatabaseFile()

        ocrFilesParentMkdirs()
        importFile(fileUri, builtinPHashDatabaseFile)
    }
}
