package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

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

interface DatabaseReadinessRepository {
    fun readiness(): Flow<DatabaseReadiness>

    fun missingChartInfoCount(): Flow<Int>
}

class DatabaseReadinessRepositoryImpl(
    private val songRepo: SongRepository,
    private val chartInfoRepo: ChartInfoRepository,
    private val difficultyWithSongRepo: DifficultyWithSongRepository,
) : DatabaseReadinessRepository {
    override fun missingChartInfoCount(): Flow<Int> = difficultyWithSongRepo.countMissingChartInfo()

    override fun readiness(): Flow<DatabaseReadiness> =
        combine(
            songRepo.count(),
            chartInfoRepo.count(),
            difficultyWithSongRepo.countMissingChartInfo(),
        ) { songCount, chartInfoCount, missingCount ->
            when {
                songCount == 0 -> DatabaseReadiness.EMPTY
                chartInfoCount == 0 -> DatabaseReadiness.SONGS_ONLY
                missingCount > 0 -> DatabaseReadiness.PARTIAL
                else -> DatabaseReadiness.READY
            }
        }
}
