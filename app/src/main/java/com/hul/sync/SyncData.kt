package com.hul.sync

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_table")
data class SyncData(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val jsonData: String,
    val visitNumber: Int, // Only values 1, 2, or 3,
    val eDiceCode: String,
    val formData: String
)

