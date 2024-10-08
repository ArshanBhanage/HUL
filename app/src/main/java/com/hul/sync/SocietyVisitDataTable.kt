package com.hul.sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "society_visit_data_table")
data class SocietyVisitDataTable(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jsonData: String,
    val visitNumber: Int, // Only values 1, 2, or 3,
    val locationName: String,
    val wingNumber : String,
    val floor : String,
    val flatNumber : String,
    val locationId : String,
)

