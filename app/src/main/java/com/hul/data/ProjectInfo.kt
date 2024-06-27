package com.hul.data

data class ProjectInfo(
    var id: Int? = null,
    var visit_id: Int? = null,
    var location_id: String? = null,
    var location_name: String? = null,
    var project_name: String? = null,
    var is_revisit: Int? = null,
    var visit_status: String? = null,
    var number_of_books_distributed: String? = null,
    var lattitude: String? = null,
    var longitude: String? = null,
    var visit_number: String? = null,
    var localString: String = "",
)
