package com.hul.sync

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class VisitDataRepository @Inject constructor(private val visitDataDao: VisitDataDao) {

    val allVisitDataTable: Flow<List<VisitDataTable>> = visitDataDao.getAllSyncData()

    suspend fun insert(visitDataTable: VisitDataTable) {
        visitDataDao.insert(visitDataTable)
    }

    suspend fun getSyncDataByVisitNumber(visitNumber: Int, uDiceCode : String): List<VisitDataTable> {
        return visitDataDao.getSyncDataByVisitNumber(visitNumber, uDiceCode)
    }

    suspend fun deleteById(id: Int) {
        visitDataDao.deleteById(id)
    }
}

