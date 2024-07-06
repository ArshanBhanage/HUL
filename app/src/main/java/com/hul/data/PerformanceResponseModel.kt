package com.hul.data

data class Response(
    val error: Boolean,
    val message: String,
    val data: PerformanceData
)

data class PerformanceData(
    val till_date: TillDate,
    val today: TillDate,
    val yesterday: TillDate,
    val this_week: TillDate,
    val this_month: TillDate,
)

data class TillDate(
    val total_visits: Int,
    val attendance: Double,
    val audit_approval: Double
)
