package com.hul.data

data class StateResponseModel(
    val error: Boolean,
    val message: String,
    val data: List<State>
)


data class State(
    val location_state: String
)
