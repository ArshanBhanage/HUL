package com.hul.data

data class SchoolActivityRequestModel(
    val visit_id: Int,
    val collected_by: String,
    val visitData: SchoolVisitData
)

data class SchoolVisitData(
    val no_of_teachers_trained: VisitDetails?,
    val picture_of_school_name: VisitDetails?,
    val selfie_with_school_name_or_u_dice_code: VisitDetails?,
    val picture_of_acknowledgement_letter: VisitDetails?,
    val number_of_students_as_per_record: VisitDetails?,
    val number_of_books_distributed: VisitDetails?,
    val school_closed: VisitDetails?,
    val school_representative_who_collected_the_books: VisitDetails?,
    val principal_contact_details: VisitDetails?,
    val principal: VisitDetails?
)

data class VisitDetails(
    val value: Any?,
    val is_approved: Int? = null,
    val rejection_reason: String? = null
)
