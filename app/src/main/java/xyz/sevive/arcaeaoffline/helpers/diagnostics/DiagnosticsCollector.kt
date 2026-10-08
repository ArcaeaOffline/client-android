package xyz.sevive.arcaeaoffline.helpers.diagnostics

import android.content.Context
import android.os.Build
import androidx.lifecycle.asFlow
import androidx.room.RoomDatabase
import androidx.room.useWriterConnection
import androidx.work.WorkManager
import co.touchlab.kermit.Logger
import com.akuleshov7.ktoml.Toml
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import xyz.sevive.arcaeaoffline.BuildConfig
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.data.OcrDependencyPaths
import xyz.sevive.arcaeaoffline.database.AppDatabase
import xyz.sevive.arcaeaoffline.database.OcrQueueDatabase
import xyz.sevive.arcaeaoffline.database.daos.OcrQueueTaskDao
import xyz.sevive.arcaeaoffline.database.entities.OcrQueueTaskStatus
import xyz.sevive.arcaeaoffline.datastore.AppPreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.OcrQueuePreferencesRepository
import xyz.sevive.arcaeaoffline.datastore.UnstableFlavorPreferencesRepository
import xyz.sevive.arcaeaoffline.jobs.ImageHashesDatabaseBuilderJob
import xyz.sevive.arcaeaoffline.jobs.OcrQueueProcessingJob
import xyz.sevive.arcaeaoffline.jobs.OcrQueueStagingJob
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.time.Clock

/**
 * Collects diagnostic information into a zip file for issue reports.
 *
 * Contents are whitelisted on purpose: no user content (task details, image URIs)
 * and no free-form settings (e.g. the custom resources API base URL) may end up in the bundle.
 */
class DiagnosticsCollector(
    private val context: Context,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val ocrQueuePreferencesRepository: OcrQueuePreferencesRepository,
    private val unstableFlavorPreferencesRepository: UnstableFlavorPreferencesRepository,
    private val ocrQueueTaskDao: OcrQueueTaskDao,
) {
    private val logger = Logger.withTag("DiagnosticsCollector")

    suspend fun collect(): File =
        withContext(Dispatchers.IO) {
            val fileName = "diagnostics-${timestamp()}.zip"
            val zipFile = File(context.cacheDir, fileName)

            ZipOutputStream(BufferedOutputStream(FileOutputStream(zipFile))).use { zip ->
                putTomlSection(zip, "manifest.toml") { Toml.encodeToString(manifestSection()) }
                putTomlSection(zip, "device.toml") { Toml.encodeToString(deviceSection()) }
                putTomlSection(zip, "preferences.toml") { Toml.encodeToString(preferencesSection()) }
                putTomlSection(zip, "ocr-state.toml") { Toml.encodeToString(ocrStateSection()) }
                putTomlSection(zip, "databases.toml") { Toml.encodeToString(databasesSection()) }
                putTomlSection(zip, "storage.toml") { Toml.encodeToString(storageSection()) }

                putAppLogFiles(zip)
                putText(zip, "logs/logcat.txt", logcatSnapshot())
            }

            zipFile
        }

    /**
     * Collects the bundle into a zip and copies it into [outputDir], returning
     * the exported file.
     */
    suspend fun exportTo(outputDir: PlatformFile): PlatformFile =
        withContext(Dispatchers.IO) {
            try {
                val zipFile = collect()
                val target = PlatformFile(outputDir, zipFile.name)
                PlatformFile(zipFile).copyTo(target)
                zipFile.delete()

                logger.i { "Diagnostics bundle exported: ${target.name}" }
                target
            } catch (e: Exception) {
                logger.e(e) { "Diagnostics export failed" }
                throw e
            }
        }

    /**
     * A failing section must not fail the whole bundle: it degrades to a comment
     * noting the error instead.
     */
    private suspend fun putTomlSection(
        zip: ZipOutputStream,
        entryName: String,
        section: suspend () -> String,
    ) {
        val content =
            try {
                section()
            } catch (e: Exception) {
                logger.e(e) { "Failed collecting section $entryName" }
                "# $entryName collection failed: ${e.message}"
            }

        putText(zip, entryName, content)
    }

    private fun timestamp(): String = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())

    private fun putText(
        zip: ZipOutputStream,
        entryName: String,
        content: String,
    ) {
        zip.putNextEntry(ZipEntry(entryName))
        zip.write(content.toByteArray(Charsets.UTF_8))
        zip.closeEntry()
    }

    private fun manifestSection(): DiagnosticsBundleManifest =
        DiagnosticsBundleManifest(
            bundle =
                DiagnosticsBundleManifest.Bundle(
                    version = BUNDLE_VERSION,
                    generatedAt = Clock.System.now().toString(),
                ),
        )

    private fun deviceSection(): DeviceSection =
        DeviceSection(
            app =
                DeviceSection.App(
                    applicationId = BuildConfig.APPLICATION_ID,
                    versionName = BuildConfig.VERSION_NAME,
                    versionCode = BuildConfig.VERSION_CODE,
                    flavor = BuildConfig.FLAVOR,
                    buildType = BuildConfig.BUILD_TYPE,
                ),
            device =
                DeviceSection.Device(
                    androidVersion = Build.VERSION.RELEASE,
                    androidSdkInt = Build.VERSION.SDK_INT,
                    manufacturer = Build.MANUFACTURER,
                    brand = Build.BRAND,
                    model = Build.MODEL,
                    locale = Locale.getDefault().toLanguageTag(),
                ),
        )

    private suspend fun preferencesSection(): PreferencesSection {
        val app = appPreferencesRepository.preferencesFlow.first()
        val ocrQueue = ocrQueuePreferencesRepository.preferencesFlow.first()
        val unstable = unstableFlavorPreferencesRepository.preferencesFlow.first()

        return PreferencesSection(
            app = PreferencesSection.App(autoSendCrashReports = app.autoSendCrashReports),
            ocrQueue =
                PreferencesSection.OcrQueue(
                    checkIsImage = ocrQueue.checkIsImage,
                    checkIsArcaeaImage = ocrQueue.checkIsArcaeaImage,
                    parallelCount = ocrQueue.parallelCount,
                ),
            unstableFlavor =
                PreferencesSection.UnstableFlavor(unstableAlertRead = unstable.unstableAlertRead),
        )
    }

    private suspend fun ocrStateSection(): OcrStateSection {
        val dependencies = OcrDependencyPaths()

        // The OCR queue DAO belongs to a Room singleton that deleteOcrQueueDatabase
        // closes; skip counting when the database file is gone rather than querying
        // the closed instance.
        val queueDbFile = File(context.getDatabasePath(OcrQueueDatabase.DATABASE_FILENAME).path)
        val taskCounts =
            if (queueDbFile.exists()) {
                runCatching {
                    OcrQueueTaskStatus.entries.associateWith { status ->
                        ocrQueueTaskDao.countByStatus(listOf(status)).first()
                    }
                }.onFailure { e ->
                    logger.e(e) { "Failed counting OCR queue tasks" }
                }.getOrNull()
            } else {
                null
            }

        return OcrStateSection(
            ocrDependencies =
                OcrStateSection.OcrDependencies(
                    phashDatabase = fileState(dependencies.phashDatabaseFile),
                    imageHashesDatabase = fileState(dependencies.imageHashesDatabaseFile),
                ),
            ocrQueueTasks =
                OcrStateSection.OcrQueueTaskCounts(
                    exists = taskCounts != null,
                    total = taskCounts?.values?.sum() ?: 0,
                    idle = taskCounts?.getValue(OcrQueueTaskStatus.IDLE) ?: 0,
                    error = taskCounts?.getValue(OcrQueueTaskStatus.ERROR) ?: 0,
                    processing = taskCounts?.getValue(OcrQueueTaskStatus.PROCESSING) ?: 0,
                    done = taskCounts?.getValue(OcrQueueTaskStatus.DONE) ?: 0,
                ),
            workmanager =
                OcrStateSection.WorkManagerStates(
                    ocrQueueProcessing = workInfoState(OcrQueueProcessingJob.WORK_NAME),
                    ocrQueueStaging = workInfoState(OcrQueueStagingJob.WORK_NAME),
                    imageHashesDatabaseBuilder = workInfoState(ImageHashesDatabaseBuilderJob.NAME),
                ),
        )
    }

    private suspend fun databasesSection(): DatabasesSection =
        DatabasesSection(
            arcaeaOffline =
                databaseInfo(
                    dbFile = File(context.getDatabasePath(ArcaeaOfflineDatabase.DATABASE_FILENAME).path),
                ) {
                    ArcaeaOfflineDatabase.getDatabase(context)
                },
            appData =
                databaseInfo(
                    dbFile = File(context.getDatabasePath(AppDatabase.DATABASE_FILENAME).path),
                ) {
                    AppDatabase.getDatabase(context)
                },
            ocrQueue =
                databaseInfo(
                    dbFile = File(context.getDatabasePath(OcrQueueDatabase.DATABASE_FILENAME).path),
                ) {
                    OcrQueueDatabase.getDatabase(context)
                },
        )

    private fun fileState(path: kotlinx.io.files.Path): FileState {
        val file = File(path.toString())
        return FileState(exists = file.exists(), sizeBytes = file.length())
    }

    private suspend fun workInfoState(workName: String): String =
        WorkManager
            .getInstance(context)
            .getWorkInfosForUniqueWorkLiveData(workName)
            .asFlow()
            .first()
            .firstOrNull()
            ?.state
            ?.toString()
            ?: "NONE"

    /**
     * Opens the database only when its file already exists, so that generating
     * diagnostics on a fresh installation does not create empty database files.
     *
     * A database that exists but cannot be opened degrades to an error-marked
     * entry instead of failing the section.
     */
    private suspend fun databaseInfo(
        dbFile: File,
        openDatabase: () -> RoomDatabase,
    ): DatabasesSection.DatabaseInfo =
        try {
            databaseInfoInternal(dbFile, openDatabase)
        } catch (e: Exception) {
            logger.e(e) { "Failed collecting database info: ${dbFile.name}" }
            DatabasesSection.DatabaseInfo(exists = dbFile.exists(), error = e.message ?: "unknown error")
        }

    private suspend fun databaseInfoInternal(
        dbFile: File,
        openDatabase: () -> RoomDatabase,
    ): DatabasesSection.DatabaseInfo {
        if (!dbFile.exists()) {
            return DatabasesSection.DatabaseInfo(exists = false)
        }

        val database = openDatabase()
        return database.useWriterConnection { connection ->
            val schemaVersion =
                connection.usePrepared("PRAGMA user_version") { statement ->
                    if (statement.step()) statement.getLong(0) else 0L
                }

            val tableNames = mutableListOf<String>()
            connection.usePrepared(
                "SELECT name FROM sqlite_master WHERE type = 'table' " +
                    "AND name NOT LIKE 'android_%' AND name NOT LIKE 'sqlite_%'",
            ) { statement ->
                while (statement.step()) {
                    tableNames.add(statement.getText(0))
                }
            }

            val tableCounts =
                tableNames.associateWith { tableName ->
                    connection.usePrepared(
                        "SELECT COUNT(*) FROM \"${tableName.replace("\"", "\"\"")}\"",
                    ) { statement ->
                        if (statement.step()) statement.getLong(0) else 0L
                    }
                }

            DatabasesSection.DatabaseInfo(
                exists = true,
                schemaVersion = schemaVersion,
                fileSizeBytes = dbFile.length(),
                tableRows = tableCounts,
            )
        }
    }

    private fun storageSection(): StorageSection {
        val filesDir = context.filesDir
        val cacheDir = context.cacheDir

        return StorageSection(
            storage =
                StorageSection.Storage(
                    filesDirSizeBytes = filesDir.recursiveSize(),
                    cacheDirSizeBytes = cacheDir.recursiveSize(),
                ),
            filesDirTopLevel =
                filesDir
                    .listFiles()
                    ?.sortedBy { it.name }
                    ?.associate {
                        it.name to if (it.isDirectory) it.recursiveSize() else it.length()
                    } ?: emptyMap(),
        )
    }

    private fun File.recursiveSize(): Long = walkTopDown().filter { it.isFile }.sumOf { it.length() }

    private fun putAppLogFiles(zip: ZipOutputStream) {
        val logsDir = File(context.filesDir, "logs")
        logsDir
            .listFiles { file -> file.isFile && file.extension == "log" }
            ?.sortedBy { it.name }
            ?.forEach { logFile ->
                zip.putNextEntry(ZipEntry("logs/${logFile.name}"))
                logFile.inputStream().use { input -> input.copyTo(zip) }
                zip.closeEntry()
            }
    }

    private fun logcatSnapshot(): String =
        try {
            val process =
                Runtime
                    .getRuntime()
                    .exec(arrayOf("logcat", "-d", "-v", "time", "-b", "main", "-b", "system", "-b", "crash"))

            val output = process.inputStream.use { it.readBytes() }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!process.waitFor(10, TimeUnit.SECONDS)) process.destroyForcibly()
            } else {
                // logcat -d exits on its own once the buffers are dumped; timed
                // waitFor is API 26+, so older devices block until exit instead.
                process.waitFor()
            }

            val text = output.toString(Charsets.UTF_8)
            if (text.length > LOGCAT_MAX_CHARS) {
                "…(truncated)\n" + text.takeLast(LOGCAT_MAX_CHARS)
            } else {
                text
            }
        } catch (e: Exception) {
            "logcat snapshot failed: $e"
        }

    companion object {
        const val BUNDLE_VERSION = 1

        private const val LOGCAT_MAX_CHARS = 2 * 1024 * 1024
    }
}

@Serializable
private data class DiagnosticsBundleManifest(
    val bundle: Bundle,
) {
    @Serializable
    data class Bundle(
        val version: Int,
        @SerialName("generated_at") val generatedAt: String,
    )
}

@Serializable
private data class DeviceSection(
    val app: App,
    val device: Device,
) {
    @Serializable
    data class App(
        @SerialName("application_id") val applicationId: String,
        @SerialName("version_name") val versionName: String,
        @SerialName("version_code") val versionCode: Int,
        val flavor: String,
        @SerialName("build_type") val buildType: String,
    )

    @Serializable
    data class Device(
        @SerialName("android_version") val androidVersion: String,
        @SerialName("android_sdk_int") val androidSdkInt: Int,
        val manufacturer: String,
        val brand: String,
        val model: String,
        val locale: String,
    )
}

@Serializable
private data class PreferencesSection(
    val app: App,
    @SerialName("ocr_queue") val ocrQueue: OcrQueue,
    @SerialName("unstable_flavor") val unstableFlavor: UnstableFlavor,
) {
    @Serializable
    data class App(
        @SerialName("auto_send_crash_reports") val autoSendCrashReports: Boolean,
    )

    @Serializable
    data class OcrQueue(
        @SerialName("check_is_image") val checkIsImage: Boolean,
        @SerialName("check_is_arcaea_image") val checkIsArcaeaImage: Boolean,
        @SerialName("parallel_count") val parallelCount: Int,
    )

    @Serializable
    data class UnstableFlavor(
        @SerialName("unstable_alert_read") val unstableAlertRead: Boolean,
    )
}

@Serializable
private data class OcrStateSection(
    @SerialName("ocr_dependencies") val ocrDependencies: OcrDependencies,
    @SerialName("ocr_queue_tasks") val ocrQueueTasks: OcrQueueTaskCounts,
    val workmanager: WorkManagerStates,
) {
    @Serializable
    data class OcrDependencies(
        @SerialName("phash_database") val phashDatabase: FileState,
        @SerialName("image_hashes_database") val imageHashesDatabase: FileState,
    )

    @Serializable
    data class OcrQueueTaskCounts(
        val exists: Boolean,
        val total: Int = 0,
        val idle: Int = 0,
        val error: Int = 0,
        val processing: Int = 0,
        val done: Int = 0,
    )

    @Serializable
    data class WorkManagerStates(
        @SerialName("ocr_queue_processing") val ocrQueueProcessing: String,
        @SerialName("ocr_queue_staging") val ocrQueueStaging: String,
        @SerialName("image_hashes_database_builder") val imageHashesDatabaseBuilder: String,
    )
}

@Serializable
private data class FileState(
    val exists: Boolean,
    @SerialName("size_bytes") val sizeBytes: Long,
)

@Serializable
private data class DatabasesSection(
    @SerialName("arcaea_offline") val arcaeaOffline: DatabaseInfo,
    @SerialName("app_data") val appData: DatabaseInfo,
    @SerialName("ocr_queue") val ocrQueue: DatabaseInfo,
) {
    @Serializable
    data class DatabaseInfo(
        val exists: Boolean,
        @SerialName("schema_version") val schemaVersion: Long = 0,
        @SerialName("file_size_bytes") val fileSizeBytes: Long = 0,
        @SerialName("table_rows") val tableRows: Map<String, Long> = emptyMap(),
        val error: String? = null,
    )
}

@Serializable
private data class StorageSection(
    val storage: Storage,
    @SerialName("files_dir_top_level") val filesDirTopLevel: Map<String, Long>,
) {
    @Serializable
    data class Storage(
        @SerialName("files_dir_size_bytes") val filesDirSizeBytes: Long,
        @SerialName("cache_dir_size_bytes") val cacheDirSizeBytes: Long,
    )
}
