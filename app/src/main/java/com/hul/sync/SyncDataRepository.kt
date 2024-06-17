package com.hul.sync

import javax.inject.Inject

class SyncDataRepository @Inject constructor(private val syncDataDao: SyncDataDao) {
    suspend fun insert(syncData: SyncData) {
        syncDataDao.insert(syncData)
    }

    suspend fun getSyncDataByVisitNumber(visitNumber: Int): List<SyncData> {
        return syncDataDao.getSyncDataByVisitNumber(visitNumber)
    }

    suspend fun deleteById(id: Int) {
        syncDataDao.deleteById(id)
    }
}

