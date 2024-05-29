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

    val packRepo: PackRepository
    val songRepo: SongRepository
    val difficultyRepo: DifficultyRepository
    val chartInfoRepo: ChartInfoRepository
    val chartRepo: ChartRepository

    val scoreRepo: ScoreRepository
    val scoreCalculatedRepo: ScoreCalculatedRepository
    val scoreBestRepo: ScoreBestRepository
    val calculatedPotentialRepo: CalculatedPotentialRepository
}

class ArcaeaOfflineDatabaseRepositoryContainerImpl(private val context: Context) :
    ArcaeaOfflineDatabaseRepositoryContainer {

    override val propertyRepository: PropertyRepository by lazy {
        PropertyRepositoryImpl(ArcaeaOfflineDatabase.getDatabase(context).propertyDao())
    }

    override val packRepo: PackRepository by lazy {
        PackRepositoryImpl(ArcaeaOfflineDatabase.getDatabase(context).packDao())
    }

    override val songRepo: SongRepository by lazy {
        SongRepositoryImpl(ArcaeaOfflineDatabase.getDatabase(context).songDao())
    }

    override val difficultyRepo: DifficultyRepository by lazy {
        DifficultyRepositoryImpl(ArcaeaOfflineDatabase.getDatabase(context).difficultyDao())
    }

    override val chartInfoRepo: ChartInfoRepository by lazy {
        ChartInfoRepositoryImpl(ArcaeaOfflineDatabase.getDatabase(context).chartInfoDao())
    }

    override val chartRepo: ChartRepository by lazy {
        ChartRepositoryImpl(ArcaeaOfflineDatabase.getDatabase(context).chartDao())
    }

    override val scoreRepo: ScoreRepository by lazy {
        ScoreRepositoryImpl(ArcaeaOfflineDatabase.getDatabase(context).scoreDao())
    }

    override val scoreCalculatedRepo: ScoreCalculatedRepository by lazy {
        ScoreCalculatedRepositoryImpl(
            ArcaeaOfflineDatabase.getDatabase(context).scoreCalculatedDao()
        )
    }

    override val scoreBestRepo: ScoreBestRepository by lazy {
        ScoreBestRepositoryImpl(
            ArcaeaOfflineDatabase.getDatabase(context).scoreBestDao()
        )
    }

    override val calculatedPotentialRepo: CalculatedPotentialRepository by lazy {
        CalculatedPotentialRepositoryImpl(
            ArcaeaOfflineDatabase.getDatabase(context).calculatedPotentialDao()
        )
    }
}
