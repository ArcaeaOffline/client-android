package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

enum class DatabaseReadiness {
    /** No songs imported. */
    EMPTY,

    /** Songs imported, but no chart info at all. */
    SONGS_ONLY,

    /** Chart info imported, but some difficulties have no constant. */
    PARTIAL,

    /** Every difficulty has a chart info constant. */
    READY,
}

data class DatabaseReadinessSummary(
    val readiness: DatabaseReadiness,
    val songCount: Int,
    val chartInfoCount: Int,
    val missingChartInfoCount: Int,
)

interface DatabaseReadinessRepository {
    fun readiness(): Flow<DatabaseReadiness>

    fun missingChartInfoCount(): Flow<Int>

    fun summary(): Flow<DatabaseReadinessSummary>
}

class DatabaseReadinessRepositoryImpl(
    private val songRepo: SongRepository,
    private val chartInfoRepo: ChartInfoRepository,
    private val difficultyWithSongRepo: DifficultyWithSongRepository,
) : DatabaseReadinessRepository {
    override fun missingChartInfoCount(): Flow<Int> = difficultyWithSongRepo.countMissingChartInfo()

    override fun summary(): Flow<DatabaseReadinessSummary> =
        combine(
            songRepo.count(),
            chartInfoRepo.count(),
            difficultyWithSongRepo.countMissingChartInfo(),
        ) { songCount, chartInfoCount, missingCount ->
            val readiness =
                when {
                    songCount == 0 -> DatabaseReadiness.EMPTY
                    chartInfoCount == 0 -> DatabaseReadiness.SONGS_ONLY
                    missingCount > 0 -> DatabaseReadiness.PARTIAL
                    else -> DatabaseReadiness.READY
                }

            DatabaseReadinessSummary(
                readiness = readiness,
                songCount = songCount,
                chartInfoCount = chartInfoCount,
                missingChartInfoCount = missingCount,
            )
        }

    override fun readiness(): Flow<DatabaseReadiness> = summary().map { it.readiness }
}
