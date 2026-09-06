package xyz.sevive.arcaeaoffline.core.database.externals.importers

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import xyz.sevive.arcaeaoffline.core.database.entities.ChartInfo

class ChartInfoDatabaseImporterTest {
    private lateinit var conn: SQLiteConnection

    @Before
    fun setUp() {
        conn = BundledSQLiteDriver().open(":memory:")
    }

    @After
    fun tearDown() {
        conn.close()
    }

    private fun createTable() {
        conn
            .prepare(
                "CREATE TABLE `charts_info` (" +
                    "`song_id` TEXT NOT NULL, " +
                    "`rating_class` INT NOT NULL, " +
                    "`constant` INT NOT NULL, " +
                    "`notes` INT, " +
                    "PRIMARY KEY(`song_id`, `rating_class`))",
            ).use { it.step() }
    }

    private fun insert(
        songId: String,
        ratingClass: Int,
        constant: Int,
        notes: Int?,
    ) {
        conn
            .prepare("INSERT INTO `charts_info` (`song_id`, `rating_class`, `constant`, `notes`) VALUES (?, ?, ?, ?)")
            .use { stmt ->
                stmt.bindText(1, songId)
                stmt.bindInt(2, ratingClass)
                stmt.bindInt(3, constant)
                if (notes == null) stmt.bindNull(4) else stmt.bindInt(4, notes)
                stmt.step()
            }
    }

    @Test
    fun importsRowsWithEnumMappingAndNullNotes() {
        createTable()
        insert("inkarusi", ratingClass = 3, constant = 1080, notes = 1500)
        insert("testsong", ratingClass = 0, constant = 410, notes = null)

        val result = ChartInfoDatabaseImporter.chartInfo(conn)

        assertEquals(
            listOf(
                ChartInfo("inkarusi", ArcaeaRatingClass.BEYOND, constant = 1080, notes = 1500),
                ChartInfo("testsong", ArcaeaRatingClass.PAST, constant = 410, notes = null),
            ),
            result,
        )
    }

    @Test
    fun emptyTableYieldsEmptyList() {
        createTable()

        val result = ChartInfoDatabaseImporter.chartInfo(conn)

        assertEquals(emptyList<ChartInfo>(), result)
    }
}
