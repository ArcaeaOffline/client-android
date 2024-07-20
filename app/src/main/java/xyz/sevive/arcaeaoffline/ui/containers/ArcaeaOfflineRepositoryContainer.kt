package xyz.sevive.arcaeaoffline.ui.containers

import android.content.Context
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.PackRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PackRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultBestRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultBestRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultCalculatedRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultCalculatedRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PlayResultRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PotentialRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.R30EntryRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.R30EntryRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.RelationshipsRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.RelationshipsRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepositoryImpl

interface ArcaeaOfflineDatabaseRepositoryContainer {
    val propertyRepo: PropertyRepository

    val packRepo: PackRepository
    val songRepo: SongRepository
    val difficultyRepo: DifficultyRepository
    val chartInfoRepo: ChartInfoRepository
    val chartRepo: ChartRepository

    val playResultRepo: PlayResultRepository
    val playResultCalculatedRepo: PlayResultCalculatedRepository
    val playResultBestRepo: PlayResultBestRepository

    val r30EntryRepo: R30EntryRepository

    val relationshipsRepo: RelationshipsRepository
    val potentialRepo: PotentialRepository
}

class ArcaeaOfflineDatabaseRepositoryContainerImpl(context: Context) :
    ArcaeaOfflineDatabaseRepositoryContainer {

    private val db = ArcaeaOfflineDatabase.getDatabase(context)

    override val propertyRepo: PropertyRepository by lazy {
        PropertyRepositoryImpl(db.propertyDao())
    }

    override val packRepo: PackRepository by lazy {
        PackRepositoryImpl(db.packDao())
    }

    override val songRepo: SongRepository by lazy {
        SongRepositoryImpl(db.songDao())
    }

    override val difficultyRepo: DifficultyRepository by lazy {
        DifficultyRepositoryImpl(db.difficultyDao())
    }

    override val chartInfoRepo: ChartInfoRepository by lazy {
        ChartInfoRepositoryImpl(db.chartInfoDao())
    }

    override val chartRepo: ChartRepository by lazy {
        ChartRepositoryImpl(db.chartDao())
    }

    override val playResultRepo: PlayResultRepository by lazy {
        PlayResultRepositoryImpl(db.playResultDao())
    }

    override val playResultCalculatedRepo: PlayResultCalculatedRepository by lazy {
        PlayResultCalculatedRepositoryImpl(db.playResultCalculatedDao())
    }

    override val playResultBestRepo: PlayResultBestRepository by lazy {
        PlayResultBestRepositoryImpl(db.playResultBestDao())
    }

    override val r30EntryRepo: R30EntryRepository by lazy {
        R30EntryRepositoryImpl(db.r30EntryDao(), playResultRepo, chartInfoRepo, propertyRepo)
    }

    override val relationshipsRepo: RelationshipsRepository by lazy {
        RelationshipsRepositoryImpl(db.relationshipsDao())
    }

    override val potentialRepo: PotentialRepository by lazy {
        PotentialRepositoryImpl(playResultBestRepo, r30EntryRepo)
    }
}
