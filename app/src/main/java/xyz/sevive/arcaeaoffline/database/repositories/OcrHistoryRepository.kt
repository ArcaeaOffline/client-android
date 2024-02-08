package xyz.sevive.arcaeaoffline.database.repositories

import xyz.sevive.arcaeaoffline.database.daos.OcrHistoryDao
import xyz.sevive.arcaeaoffline.database.entities.OcrHistory

class OcrHistoryRepository(private val dao: OcrHistoryDao) {
    fun findAll() = dao.findAll()
    suspend fun insert(item: OcrHistory) = dao.insert(item)
}
