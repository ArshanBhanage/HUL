package com.hul.sync

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDataDao {
    @Insert
    suspend fun insert(visitDataTable: VisitDataTable)

    @Query("SELECT * FROM visit_data_table")
    fun getAllSyncData(): Flow<List<VisitDataTable>>

    @Query("SELECT * FROM visit_data_table WHERE visitNumber = :visitNumber AND uDiceCode = :uDiceCode")
    suspend fun getSyncDataByVisitNumber(visitNumber: Int, uDiceCode : String): List<VisitDataTable>

    @Query("DELETE FROM visit_data_table WHERE id = :id")
    suspend fun deleteById(id: Int)
}
