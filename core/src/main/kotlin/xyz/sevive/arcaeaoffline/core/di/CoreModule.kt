package xyz.sevive.arcaeaoffline.core.di

import android.content.Context
import org.koin.dsl.module
import org.koin.plugin.module.dsl.bind
import org.koin.plugin.module.dsl.create
import org.koin.plugin.module.dsl.single
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.daos.ChartDao
import xyz.sevive.arcaeaoffline.core.database.daos.ChartInfoDao
import xyz.sevive.arcaeaoffline.core.database.daos.DifficultyDao
import xyz.sevive.arcaeaoffline.core.database.daos.DifficultyLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.daos.MetaDao
import xyz.sevive.arcaeaoffline.core.database.daos.PackDao
import xyz.sevive.arcaeaoffline.core.database.daos.PackLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.daos.PlayResultDao
import xyz.sevive.arcaeaoffline.core.database.daos.PropertyDao
import xyz.sevive.arcaeaoffline.core.database.daos.R30EntryDao
import xyz.sevive.arcaeaoffline.core.database.daos.RelationshipsDao
import xyz.sevive.arcaeaoffline.core.database.daos.SongDao
import xyz.sevive.arcaeaoffline.core.database.daos.SongLocalizedDao
import xyz.sevive.arcaeaoffline.core.database.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyLocalizedRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyLocalizedRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.MetaRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.MetaRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.PackLocalizedRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.PackLocalizedRepositoryImpl
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
import xyz.sevive.arcaeaoffline.core.database.repositories.SongLocalizedRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongLocalizedRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.SongRepositoryImpl
import xyz.sevive.arcaeaoffline.core.ocr.ImageHashers
import xyz.sevive.arcaeaoffline.core.ocr.device.DeviceOcrOnnxHelper
import xyz.sevive.arcaeaoffline.core.ocr.device.ScreenshotDetect

internal fun createArcaeaOfflineDatabase(context: Context) = ArcaeaOfflineDatabase.getDatabase(context)

internal fun metaDao(db: ArcaeaOfflineDatabase) = db.metaDao()

internal fun propertyDao(db: ArcaeaOfflineDatabase) = db.propertyDao()

internal fun packDao(db: ArcaeaOfflineDatabase) = db.packDao()

internal fun packLocalizedDao(db: ArcaeaOfflineDatabase) = db.packLocalizedDao()

internal fun songDao(db: ArcaeaOfflineDatabase) = db.songDao()

internal fun songLocalizedDao(db: ArcaeaOfflineDatabase) = db.songLocalizedDao()

internal fun difficultyDao(db: ArcaeaOfflineDatabase) = db.difficultyDao()

internal fun difficultyLocalizedDao(db: ArcaeaOfflineDatabase) = db.difficultyLocalizedDao()

internal fun chartInfoDao(db: ArcaeaOfflineDatabase) = db.chartInfoDao()

internal fun playResultDao(db: ArcaeaOfflineDatabase) = db.playResultDao()

internal fun relationshipsDao(db: ArcaeaOfflineDatabase) = db.relationshipsDao()

internal fun r30EntryDao(db: ArcaeaOfflineDatabase) = db.r30EntryDao()

internal fun chartDao(db: ArcaeaOfflineDatabase) = db.chartDao()

val coreModule =
    module {
        single<ArcaeaOfflineDatabase> { create(::createArcaeaOfflineDatabase) }

        single<MetaDao> { create(::metaDao) }
        single<PropertyDao> { create(::propertyDao) }
        single<PackDao> { create(::packDao) }
        single<PackLocalizedDao> { create(::packLocalizedDao) }
        single<SongDao> { create(::songDao) }
        single<SongLocalizedDao> { create(::songLocalizedDao) }
        single<DifficultyDao> { create(::difficultyDao) }
        single<DifficultyLocalizedDao> { create(::difficultyLocalizedDao) }
        single<ChartInfoDao> { create(::chartInfoDao) }
        single<PlayResultDao> { create(::playResultDao) }
        single<RelationshipsDao> { create(::relationshipsDao) }
        single<R30EntryDao> { create(::r30EntryDao) }
        single<ChartDao> { create(::chartDao) }

        single<MetaRepositoryImpl>().bind(MetaRepository::class)
        single<PropertyRepositoryImpl>().bind(PropertyRepository::class)
        single<PackRepositoryImpl>().bind(PackRepository::class)
        single<PackLocalizedRepositoryImpl>().bind(PackLocalizedRepository::class)
        single<SongRepositoryImpl>().bind(SongRepository::class)
        single<SongLocalizedRepositoryImpl>().bind(SongLocalizedRepository::class)
        single<DifficultyRepositoryImpl>().bind(DifficultyRepository::class)
        single<DifficultyLocalizedRepositoryImpl>().bind(DifficultyLocalizedRepository::class)
        single<ChartInfoRepositoryImpl>().bind(ChartInfoRepository::class)
        single<ChartRepositoryImpl>().bind(ChartRepository::class)
        single<PlayResultRepositoryImpl>().bind(PlayResultRepository::class)
        single<PlayResultCalculatedRepositoryImpl>().bind(PlayResultCalculatedRepository::class)
        single<PlayResultBestRepositoryImpl>().bind(PlayResultBestRepository::class)
        single<R30EntryRepositoryImpl>().bind(R30EntryRepository::class)
        single<RelationshipsRepositoryImpl>().bind(RelationshipsRepository::class)
        single<PotentialRepositoryImpl>().bind(PotentialRepository::class)

        // TODO: evaluate these usages
        single { ChartFactory }
        single { DeviceOcrOnnxHelper }
        single { ImageHashers }
        single { ScreenshotDetect }
    }
