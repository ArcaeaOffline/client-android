package xyz.sevive.arcaeaoffline.core.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.helpers.ChartFactory
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartInfoRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.ChartRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyLocalizedRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyLocalizedRepositoryImpl
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepository
import xyz.sevive.arcaeaoffline.core.database.repositories.DifficultyRepositoryImpl
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

val coreModule =
    module {
        single { ArcaeaOfflineDatabase.getDatabase(androidContext()) }

        single { get<ArcaeaOfflineDatabase>().propertyDao() }
        single { get<ArcaeaOfflineDatabase>().packDao() }
        single { get<ArcaeaOfflineDatabase>().packLocalizedDao() }
        single { get<ArcaeaOfflineDatabase>().songDao() }
        single { get<ArcaeaOfflineDatabase>().songLocalizedDao() }
        single { get<ArcaeaOfflineDatabase>().difficultyDao() }
        single { get<ArcaeaOfflineDatabase>().difficultyLocalizedDao() }
        single { get<ArcaeaOfflineDatabase>().chartInfoDao() }
        single { get<ArcaeaOfflineDatabase>().playResultDao() }
        single { get<ArcaeaOfflineDatabase>().relationshipsDao() }
        single { get<ArcaeaOfflineDatabase>().r30EntryDao() }
        single { get<ArcaeaOfflineDatabase>().chartDao() }

        single<PropertyRepositoryImpl>() bind PropertyRepository::class
        single<PackRepositoryImpl>() bind PackRepository::class
        single<PackLocalizedRepositoryImpl>() bind PackLocalizedRepository::class
        single<SongRepositoryImpl>() bind SongRepository::class
        single<SongLocalizedRepositoryImpl>() bind SongLocalizedRepository::class
        single<DifficultyRepositoryImpl>() bind DifficultyRepository::class
        single<DifficultyLocalizedRepositoryImpl>() bind DifficultyLocalizedRepository::class
        single<ChartInfoRepositoryImpl>() bind ChartInfoRepository::class
        single<ChartRepositoryImpl>() bind ChartRepository::class
        single<PlayResultRepositoryImpl>() bind PlayResultRepository::class
        single<PlayResultCalculatedRepositoryImpl>() bind PlayResultCalculatedRepository::class
        single<PlayResultBestRepositoryImpl>() bind PlayResultBestRepository::class
        single<R30EntryRepositoryImpl>() bind R30EntryRepository::class
        single<RelationshipsRepositoryImpl>() bind RelationshipsRepository::class
        single<PotentialRepositoryImpl>() bind PotentialRepository::class

        single { ChartFactory }
        single { DeviceOcrOnnxHelper }
        single { ImageHashers }
        single { ScreenshotDetect }
    }
