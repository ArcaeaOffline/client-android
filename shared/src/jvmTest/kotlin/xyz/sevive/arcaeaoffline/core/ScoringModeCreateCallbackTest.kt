package xyz.sevive.arcaeaoffline.core

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import xyz.sevive.arcaeaoffline.core.database.ArcaeaOfflineDatabase
import xyz.sevive.arcaeaoffline.core.database.ScoringModeCreateCallback
import xyz.sevive.arcaeaoffline.core.database.entities.Property
import xyz.sevive.arcaeaoffline.core.database.repositories.PropertyRepository
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ScoringModeCreateCallbackTest {
    private lateinit var db: ArcaeaOfflineDatabase

    @BeforeTest
    fun setUp() {
        db =
            Room
                .inMemoryDatabaseBuilder<ArcaeaOfflineDatabase>()
                .setDriver(BundledSQLiteDriver())
                .addCallback(ScoringModeCreateCallback)
                .build()
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    @Test
    fun creationStoresTheScoringMode() {
        val property = runBlocking { db.propertyDao().find(Property.KEY_SCORING_MODE).first() }

        assertEquals(PropertyRepository.DEFAULT_SCORING_MODE.key.toString(), property?.value)
    }
}
