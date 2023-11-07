package xyz.sevive.arcaeaoffline.core.database.repositories

import kotlinx.coroutines.flow.Flow
import xyz.sevive.arcaeaoffline.core.database.daos.CalculatedPotentialDao

interface CalculatedPotentialRepository {
    fun b30(): Flow<Double>
}

class CalculatedPotentialRepositoryImpl(private val dao: CalculatedPotentialDao) :
    CalculatedPotentialRepository {
    override fun b30(): Flow<Double> = dao.b30()
}
