package com.hul.data

/**
 * Created by Nitin Chorge on 20-06-2024.
 */
data class FormElement(

    val visit_template_id: Int,
    val form_field_title: String,
    val form_field_subtitle: String,
    val form_field_description: String,
    val form_field_conversion_type: String,
    val input_type: String,
    val input_regex: String? = null,
    val input_allowed_values: String,
    val input_default: String,
    val input_description: String,
    val is_optional: Int,
    val is_editable: Int,
)
