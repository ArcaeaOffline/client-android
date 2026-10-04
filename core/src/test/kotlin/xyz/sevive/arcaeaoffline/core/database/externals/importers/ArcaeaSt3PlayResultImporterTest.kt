package xyz.sevive.arcaeaoffline.core.database.externals.importers

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.datetime.LocalDate
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultClearType
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaPlayResultModifier
import xyz.sevive.arcaeaoffline.core.constants.ArcaeaRatingClass
import kotlin.time.Instant

class ArcaeaSt3PlayResultImporterTest {
    @Test
    fun fixSt3TimestampPadsTruncatedAndDropsInvalid() {
        assertEquals(null, fixSt3Timestamp(null))
        assertEquals(1788385000L, fixSt3Timestamp(1788385))
        assertEquals(1566000000L, fixSt3Timestamp(1566))
        assertEquals(null, fixSt3Timestamp(0))
        assertEquals(null, fixSt3Timestamp(12000))
        assertEquals(1489017600L, fixSt3Timestamp(1489017600))
        assertEquals(1789000000L, fixSt3Timestamp(1789000000))
    }

    @Test
    fun toPlayResultMapsEnumsAndStampsComment() {
        val result =
            St3PlayResult(
                songId = "a",
                ratingClass = 3,
                score = 9900000,
                pure = 900,
                far = 50,
                lost = 0,
                date = 1788385000,
                modifier = 2,
                clearType = 2,
            ).toPlayResult(LocalDate(2026, 1, 2))

        assertEquals("a", result.songId)
        assertEquals(ArcaeaRatingClass.BEYOND, result.ratingClass)
        assertEquals(9900000, result.score)
        assertEquals(900, result.pure)
        assertEquals(50, result.far)
        assertEquals(0, result.lost)
        assertEquals(Instant.fromEpochSeconds(1788385000), result.date)
        assertEquals(ArcaeaPlayResultModifier.HARD, result.modifier)
        assertEquals(ArcaeaPlayResultClearType.FULL_RECALL, result.clearType)
        assertEquals("Imported from st3 at 2026-01-02", result.comment)
    }

    private lateinit var conn: SQLiteConnection

    @Before
    fun setUp() {
        conn = BundledSQLiteDriver().open(":memory:")
    }

    @After
    fun tearDown() {
        conn.close()
    }

    private val seedSql: String by lazy {
        javaClass
            .getResourceAsStream("/xyz/sevive/arcaeaoffline/core/database/externals/importers/st3_seed.sql")!!
            .bufferedReader()
            .use { it.readText() }
    }

    private fun execScript(script: String) {
        script
            .lineSequence()
            .filterNot { it.trimStart().startsWith("--") }
            .joinToString("\n")
            .split(';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { statement ->
                conn.prepare(statement).use { it.step() }
            }
    }

    private data class ImportedRow(
        val songId: String,
        val ratingClass: ArcaeaRatingClass,
        val score: Int,
        val pure: Int?,
        val far: Int?,
        val lost: Int?,
        val date: Instant?,
        val modifier: ArcaeaPlayResultModifier?,
        val clearType: ArcaeaPlayResultClearType?,
        val maxRecall: Int?,
    )

    private fun List<xyz.sevive.arcaeaoffline.core.database.entities.PlayResult>.toImportedRows() =
        map {
            ImportedRow(
                it.songId,
                it.ratingClass,
                it.score,
                it.pure,
                it.far,
                it.lost,
                it.date,
                it.modifier,
                it.clearType,
                it.maxRecall,
            )
        }

    @Test
    fun importsSeedRows() {
        execScript(seedSql)

        val result = ArcaeaSt3PlayResultImporter.playResults(conn)

        assertEquals(
            listOf(
                ImportedRow(
                    "a",
                    ArcaeaRatingClass.PRESENT,
                    9900000,
                    900,
                    50,
                    0,
                    Instant.fromEpochSeconds(1788385000),
                    ArcaeaPlayResultModifier.NORMAL,
                    ArcaeaPlayResultClearType.FULL_RECALL,
                    // pure + far: the row is consistent with its FR clear type
                    950,
                ),
                ImportedRow(
                    "b",
                    ArcaeaRatingClass.PRESENT,
                    9800000,
                    800,
                    99,
                    1,
                    Instant.fromEpochSeconds(1670283375),
                    ArcaeaPlayResultModifier.HARD,
                    ArcaeaPlayResultClearType.FULL_RECALL,
                    // lost > 0: the best-score play is not the FR play
                    null,
                ),
                ImportedRow(
                    "c",
                    ArcaeaRatingClass.FUTURE,
                    10000000,
                    1000,
                    0,
                    0,
                    Instant.fromEpochSeconds(1670280000),
                    ArcaeaPlayResultModifier.EASY,
                    ArcaeaPlayResultClearType.PURE_MEMORY,
                    1000,
                ),
                ImportedRow(
                    "d",
                    ArcaeaRatingClass.FUTURE,
                    9700000,
                    900,
                    95,
                    5,
                    null,
                    ArcaeaPlayResultModifier.NORMAL,
                    ArcaeaPlayResultClearType.PURE_MEMORY,
                    // far > 0: not consistent with PM
                    null,
                ),
                ImportedRow(
                    "e",
                    ArcaeaRatingClass.PAST,
                    8000000,
                    null,
                    100,
                    50,
                    null,
                    ArcaeaPlayResultModifier.NORMAL,
                    ArcaeaPlayResultClearType.NORMAL_CLEAR,
                    null,
                ),
                ImportedRow(
                    "f",
                    ArcaeaRatingClass.BEYOND,
                    7000000,
                    700,
                    0,
                    0,
                    Instant.fromEpochSeconds(1789000000),
                    ArcaeaPlayResultModifier.NORMAL,
                    // no cleartypes row: the score survives with a null clear type
                    null,
                    null,
                ),
                ImportedRow(
                    "g",
                    ArcaeaRatingClass.BEYOND,
                    6000000,
                    600,
                    30,
                    20,
                    Instant.fromEpochSeconds(1566000000),
                    ArcaeaPlayResultModifier.NORMAL,
                    ArcaeaPlayResultClearType.TRACK_LOST,
                    null,
                ),
            ),
            result.toImportedRows(),
        )

        assertTrue(result.all { it.comment?.startsWith("Imported from st3 at ") == true })
    }

    @Test
    fun emptyTablesYieldEmptyList() {
        execScript(
            """
            CREATE TABLE scores(
              id integer primary key autoincrement not null,
              version int, score int, shinyPerfectCount int, perfectCount int,
              nearCount int, missCount int, date int, songId text,
              songDifficulty int, modifier int, health int, ct int default 0);

            CREATE TABLE cleartypes(
              id integer primary key autoincrement not null,
              songId text, songDifficulty int, clearType int, ct int default 0);
            """.trimIndent(),
        )

        assertEquals(emptyList<ImportedRow>(), ArcaeaSt3PlayResultImporter.playResults(conn).toImportedRows())
    }
}
