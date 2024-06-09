package com.hul.data

data class UserDetails(
    val mobile_number: String,
    val user_type: String,
    val user_fullname: String,
    val district: String,
    val state: String,
    val users_mapped: List<MappedUser>
)

data class MappedUser(
    val mobile_number: String,
    val user_type: String,
    val user_fullname: String,
    val user_id: Int
)