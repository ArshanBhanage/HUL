package com.hul.data

import org.json.JSONObject

/**
 * Created by Nitin Chorge on 26-11-2020.
 */
data class ResponseModel(
    var data: Map<String, Object>? = null,
    var message: String? = null,
    var error: Boolean = false,
    var createdAt: String? = null,
)