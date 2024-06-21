package com.hul.data

/**
 * Created by Nitin Chorge on 20-06-2024.
 */
data class FormElement(
    val type: String,
    val label: String,
    val placeholder: String? = null,
    val options: List<String>? = null
)
