package xyz.sevive.arcaeaoffline.core

import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.migrations.Migration_15_16
import xyz.sevive.arcaeaoffline.core.database.migrations.Migration_7_8
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path

class DatabaseMigrationsTest {
    private val testDatabaseFile = Files.createTempDirectory("migration-test").resolve("migration-test.db")

    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            schemaDirectoryPath = Path.of("schemas"),
            databasePath = testDatabaseFile,
            driver = BundledSQLiteDriver(),
            databaseClass = ArcaeaOfflineDatabase::class,
        )

    @Test
    @Throws(IOException::class)
    fun migrate7To8() =
        runBlocking {
            resetTestDatabase()
            val sqlFilename = "arcaea_offline_0.0.6.db.sql"

            helper.createDatabase(7).apply {
                // filling data
                execSqlScript(this, sqlFilename)

                // Prepare for the next version.
                close()
            }

            // perform migration
            val db = helper.runMigrationsAndValidate(8, listOf(Migration_7_8))

            /**
             * schema passed
             * now validate if the data was migrated, key points:
             * * enum key to value
             */
            val tables =
                listOf(
                    "difficulties",
                    "charts_info",
                    "play_results",
                )

            tables.forEach { table ->
                db
                    .prepare("SELECT COUNT(*) FROM $table WHERE song_id = 'inkarusi' AND rating_class = 2")
                    .use { statement ->
                        assertTrue(statement.step())
                        assertEquals(1L, statement.getLong(0))
                    }

                db
                    .prepare("SELECT COUNT(*) FROM $table WHERE song_id = 'inkarusi' AND rating_class = 'FUTURE'")
                    .use { statement ->
                        assertTrue(statement.step())
                        assertEquals(0L, statement.getLong(0))
                    }
            }

            db.close()
        }

    @Test
    @Throws(IOException::class)
    fun migrate14To15() =
        runBlocking {
            resetTestDatabase()
            helper.createDatabase(14).apply {
                // NOT NULL columns must be provided; nullable ones are omitted.
                execSQL(
                    "INSERT INTO difficulties " +
                        "(song_id, rating_class, rating, rating_plus, audio_override, jacket_override) " +
                        "VALUES ('test', 3, 110, 0, 0, 0)",
                )
                close()
            }

            val db = helper.runMigrationsAndValidate(15)

            // Old rows get a null alias.
            db
                .prepare("SELECT rating_class_alias FROM difficulties WHERE song_id = 'test'")
                .use { statement ->
                    assertTrue(statement.step())
                    assertTrue(statement.isNull(0))
                }

            // The column accepts alias values.
            db.execSQL("UPDATE difficulties SET rating_class_alias = 1 WHERE song_id = 'test'")
            db
                .prepare("SELECT rating_class_alias FROM difficulties WHERE song_id = 'test'")
                .use { statement ->
                    assertTrue(statement.step())
                    assertEquals(1L, statement.getLong(0))
                }

            // The dropped `charts` view must not linger: a future `CREATE VIEW charts`
            // would fail while the stale definition still exists.
            db
                .prepare("SELECT COUNT(*) FROM sqlite_master WHERE type = 'view' AND name = 'charts'")
                .use { statement ->
                    assertTrue(statement.step())
                    assertEquals(0L, statement.getLong(0))
                }

            db.close()
        }

    @Test
    @Throws(IOException::class)
    fun migrate15To16() =
        runBlocking {
            resetTestDatabase()
            helper.createDatabase(15).close()

            val db = helper.runMigrationsAndValidate(16, listOf(Migration_15_16))

            db
                .prepare("SELECT `value` FROM properties WHERE `key` = 'scoring_mode'")
                .use { statement ->
                    assertTrue(statement.step())
                    assertEquals("20260827", statement.getText(0))
                }

            db.close()
        }

    @Test
    @Throws(IOException::class)
    fun migrate15To16KeepsAChosenMode() =
        runBlocking {
            resetTestDatabase()
            helper.createDatabase(15).apply {
                execSQL("INSERT INTO properties (`key`, `value`) VALUES ('scoring_mode', '20170602')")
                close()
            }

            val db = helper.runMigrationsAndValidate(16, listOf(Migration_15_16))

            db
                .prepare("SELECT `value` FROM properties WHERE `key` = 'scoring_mode'")
                .use { statement ->
                    assertTrue(statement.step())
                    assertEquals("20170602", statement.getText(0))
                }

            db.close()
        }

    /**
     * All tests share one database file; drop it so each starts from a clean state.
     */
    private fun resetTestDatabase() {
        Files.deleteIfExists(testDatabaseFile)
        // The bundled driver runs in WAL mode, so stale sidecar files must not leak into the next test.
        Files.deleteIfExists(testDatabaseFile.resolveSibling("migration-test.db-wal"))
        Files.deleteIfExists(testDatabaseFile.resolveSibling("migration-test.db-shm"))
    }

    private fun execSqlScript(
        db: SQLiteConnection,
        filename: String,
    ) {
        val inputStream =
            javaClass.classLoader?.getResourceAsStream(filename)
                ?: throw IOException("SQL file not found: $filename")

        inputStream.bufferedReader().use { reader ->
            val script = reader.readText()
            script.split(";").forEach { statement ->
                val trimmed = statement.trim()
                if (trimmed.isNotEmpty()) {
                    db.execSQL(trimmed)
                }
            }
        }
    }
}
