package com.hul.data

data class PerformanceResponseModel(
    val error: Boolean,
    val message: String,
    val data: PerformanceData
)

data class PerformanceData(
    val total_visits: Int,
    val attendance: Int,
    val audit_approval: Int
)
