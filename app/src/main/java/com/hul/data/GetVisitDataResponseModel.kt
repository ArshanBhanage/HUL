package com.hul.data

data class GetVisitDataResponseModel(
    val error: Boolean,
    val message: String,
    val data: GetVisitDataResponseData
)

data class GetVisitDataResponseData(
    val visit_1: Visit1
)

data class Visit1(
    val no_of_teachers_trained: FieldData,
    val number_of_books_distributed: FieldData,
    val number_of_students_as_per_record: FieldData,
    val principal: FieldData,
    val principal_contact_details: FieldData,
    val school_closed: FieldData,
    val school_name: FieldData,
    val school_representative_who_collected_the_books: FieldData,
    val u_dice_code: FieldData,
    val nullField: FieldData?
)

data class FieldData(
    val value: String,
    val is_approved: Boolean?,
    val rejection_reason: String?,
    val is_image: Boolean
)
