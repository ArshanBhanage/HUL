package com.hul.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SocietyVisitDataDao {
    @Insert
    suspend fun insert(societyVisitDataTable: SocietyVisitDataTable)

    @Query("SELECT * FROM society_visit_data_table")
    fun getAllSyncData(): Flow<List<SocietyVisitDataTable>>

    @Query("SELECT * FROM society_visit_data_table WHERE wingNumber = :wingNumber AND floor = :floor AND flatNumber = :flatNumber")
    suspend fun getSyncDataByVisitNumber(wingNumber: String, floor : String, flatNumber : String): List<SocietyVisitDataTable>

    @Query("DELETE FROM society_visit_data_table WHERE id = :id")
    suspend fun deleteById(id: Int)
}
