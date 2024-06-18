package com.hul.data

data class GetVisitDataResponseModel(
    var error: Boolean,
    var message: String,
    var data: GetVisitDataResponseData
)

data class GetVisitDataResponseData(
    var visit_1: VisitData,
    var visit_2: VisitData,
    var visit_3: VisitData
)

data class Visit1(
    var no_of_teachers_trained: FieldData,
    var number_of_books_distributed: FieldData,
    var number_of_students_as_per_record: FieldData,
    var principal: FieldData,
    var principal_contact_details: FieldData,
    var school_closed: FieldData,
    var school_name: FieldData,
    var school_representative_who_collected_the_books: FieldData,
    var u_dice_code: FieldData,
    var nullField: FieldData?,
    var picture_of_school_with_name_visible: FieldData,
    var picture_of_school_with_unique_code: FieldData,
    var picture_of_teachers_seeing_the_video: FieldData,
    var picture_of_students_with_book_distribution: FieldData,
    var picture_of_acknowledgement_letter: FieldData,
    var name_of_the_school_representative_who_collected_the_books: FieldData,
    var mobile_number_of_the_school_representative_who_collected_the_books: FieldData,
    var name_of_the_principal: FieldData,
    var mobile_number_of_the_principal: FieldData,
    var revisit_applicable: FieldData,
    var curriculum_on_track : FieldData,
    var remark: FieldData,
)

data class FieldData(
    var value: String,
    var is_approved: Int?,
    var rejection_reason: String?,
    var is_image: Boolean
)
