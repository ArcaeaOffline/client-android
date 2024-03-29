package xyz.sevive.arcaeaoffline.ui.containers

import android.content.Context
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.repositories.CalculatedPotentialRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.CalculatedPotentialRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.PackRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PackRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.ScoreBestRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.ScoreBestRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.ScoreCalculatedRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.ScoreCalculatedRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.ScoreRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.ScoreRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepositoryImpl

interface ArcaeaOfflineDatabaseRepositoryContainer {
    val propertyRepository: PropertyRepository

    val packRepository: PackRepository
    val songRepository: SongRepository
    val difficultyRepository: DifficultyRepository
    val chartInfoRepository: ChartInfoRepository
    val chartRepository: ChartRepository

    val scoreRepository: ScoreRepository
    val scoreCalculatedRepository: ScoreCalculatedRepository
    val scoreBestRepository: ScoreBestRepository
    val calculatedPotentialRepository: CalculatedPotentialRepository
}

class ArcaeaOfflineDatabaseRepositoryContainerImpl(private val context: Context) :
    ArcaeaOfflineDatabaseRepositoryContainer {

    override val propertyRepository: PropertyRepository by lazy {
        PropertyRepositoryImpl(ArcaeaOfflineDatabase.getDatabase(context).propertyDao())
    }

    override val packRepository: PackRepository by lazy {
        PackRepositoryImpl(ArcaeaOfflineDatabase.getDatabase(context).packDao())
    }

    override val songRepository: SongRepository by lazy {
        SongRepositoryImpl(ArcaeaOfflineDatabase.getDatabase(context).songDao())
    }

    override val difficultyRepository: DifficultyRepository by lazy {
        DifficultyRepositoryImpl(ArcaeaOfflineDatabase.getDatabase(context).difficultyDao())
    }

    override val chartInfoRepository: ChartInfoRepository by lazy {
        ChartInfoRepositoryImpl(ArcaeaOfflineDatabase.getDatabase(context).chartInfoDao())
    }

    override val chartRepository: ChartRepository by lazy {
        ChartRepositoryImpl(ArcaeaOfflineDatabase.getDatabase(context).chartDao())
    }

    override val scoreRepository: ScoreRepository by lazy {
        ScoreRepositoryImpl(ArcaeaOfflineDatabase.getDatabase(context).scoreDao())
    }

    override val scoreCalculatedRepository: ScoreCalculatedRepository by lazy {
        ScoreCalculatedRepositoryImpl(
            ArcaeaOfflineDatabase.getDatabase(context).scoreCalculatedDao()
        )
    }

    override val scoreBestRepository: ScoreBestRepository by lazy {
        ScoreBestRepositoryImpl(
            ArcaeaOfflineDatabase.getDatabase(context).scoreBestDao()
        )
    }

    override val calculatedPotentialRepository: CalculatedPotentialRepository by lazy {
        CalculatedPotentialRepositoryImpl(
            ArcaeaOfflineDatabase.getDatabase(context).calculatedPotentialDao()
        )
    }
}
