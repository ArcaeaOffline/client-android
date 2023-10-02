package xyz.sevive.arcaeaoffline.settings


import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.nio.file.Files

class SettingsOcr(val context: Context) {
    val ocrFilesParent: File
        get() {
            return File(context.filesDir, "ocr")
        }

    fun knnModelFile() = File(this.ocrFilesParent.path, "knn.dat")
    fun pHashDatabaseFile() = File(this.ocrFilesParent.path, "phash.db")

    val knnModelOk: Boolean
        get() {
            return knnModelFile().exists()
        }

    fun ocrFilesParentMkdirs() {
        if (!ocrFilesParent.mkdirs()) {
            Log.w("SettingsOCR", "ocrFilesParent mkdirs failed, $ocrFilesParent")
        }
    }

    private fun importFile(src: Uri, dst: File) {
        val resolver = this.context.contentResolver
        dst.delete()

        val inputStream = resolver.openInputStream(src)
        if (inputStream != null) {
            Files.copy(inputStream, dst.toPath())
            inputStream.close()
        }
    }

    fun importKnn(fileUri: Uri) {
        val builtinKnnModelFile = knnModelFile()

        ocrFilesParentMkdirs()
        importFile(fileUri, builtinKnnModelFile)
    }
}
