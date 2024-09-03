package com.hul.data

/**
 * Created by Nitin Chorge on 17-08-2024.
 */
data class Society(
    val location_name : String? = null,
    val displayName : String? = null,
    val visit_status : String? = null,
    var localString: String = "",
    val id : Int? = null,
    var visit_id: Int? = null,
    var flats_completed: ArrayList<FlatsCompleted>? = null,
)
