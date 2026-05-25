package xyz.sevive.arcaeaoffline.core

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.migrations.Migration_7_8
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseMigrationsTest {
    private val testDatabaseName = "arcaea-offline"

    @get:Rule
    val helper: MigrationTestHelper =
        MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            ArcaeaOfflineDatabase::class.java,
        )

    @Test
    @Throws(IOException::class)
    fun migrate7To8() {
        val sqlFilename = "arcaea_offline_0.0.6.db.sql"

        var db =
            helper.createDatabase(testDatabaseName, 7).apply {
                // filling data
                execSqlScript(this, sqlFilename)

                // Prepare for the next version.
                close()
            }

        // perform migration
        db = helper.runMigrationsAndValidate(testDatabaseName, 8, true, Migration_7_8)

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
                .query("SELECT COUNT(*) FROM $table WHERE song_id = 'inkarusi' AND rating_class = 2")
                .use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(1, cursor.getInt(0))
                }

            db
                .query("SELECT COUNT(*) FROM $table WHERE song_id = 'inkarusi' AND rating_class = 'FUTURE'")
                .use { cursor ->
                    assertTrue(cursor.moveToFirst())
                    assertEquals(0, cursor.getInt(0))
                }
        }
    }

    private fun execSqlScript(
        db: SupportSQLiteDatabase,
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
