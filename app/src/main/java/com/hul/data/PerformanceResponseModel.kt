package com.hul.data

data class Response(
    val error: Boolean,
    val message: String,
    val data: PerformanceData
)

data class PerformanceData(
    val till_date: TillDate
)

data class TillDate(
    val total_visits: Int,
    val attendance: Int,
    val audit_approval: Int
)
