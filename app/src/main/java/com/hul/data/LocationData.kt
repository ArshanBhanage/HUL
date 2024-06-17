package com.hul.data

data class LocationData(
    val location_name: String,
    val area_id: Int,
    val project_id: Int,
    val location_type: String,
    val lattitude: String,
    val longitude: String,
    val external_id1: String,
    val external_id1_description: String,
    val external_id2: String,
    val external_id2_description: String,
    val location_ward: String,
    val location_district: String,
    val location_state: String,
    val remarks: String
)

