package com.hul.sync

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SocietyVisitDataRepository @Inject constructor(private val societyVisitDataDao: SocietyVisitDataDao) {

    val allVisitDataTable: Flow<List<SocietyVisitDataTable>> = societyVisitDataDao.getAllSyncData()

    suspend fun insert(societyVisitDataTable: SocietyVisitDataTable) {
        societyVisitDataDao.insert(societyVisitDataTable)
    }

    suspend fun getSyncDataByVisitNumber(wingNumber: String, floor : String, flatNumber : String): List<SocietyVisitDataTable> {
        return societyVisitDataDao.getSyncDataByVisitNumber(wingNumber, floor,flatNumber)
    }

    suspend fun deleteById(id: Int) {
        societyVisitDataDao.deleteById(id)
    }
}

