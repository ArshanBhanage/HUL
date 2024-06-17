package com.hul.data

data class DistrictResponseModel(
    val error: Boolean,
    val message: String,
    val data: List<District>
)


data class District(
    val area_id: Int,
    val area_name: String
)
