package com.hul.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SyncDataDao {
    @Insert
    suspend fun insert(syncData: SyncData)

    @Query("SELECT * FROM sync_table WHERE visitNumber = :visitNumber")
    suspend fun getSyncDataByVisitNumber(visitNumber: Int): List<SyncData>

    @Query("DELETE FROM sync_table WHERE id = :id")
    suspend fun deleteById(id: Int)
}
