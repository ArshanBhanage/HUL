package com.hul.data

data class VisitData(
    var picture_of_school_with_name_visible: String? = null,
    var picture_of_school_with_unique_code: String? = null,
    var picture_of_students_with_book_distribution: String? = null,
    var picture_of_students_with_book_distribution2: String? = null,
    var picture_of_students_with_book_distribution3: String? = null,
    var picture_of_teachers_seeing_the_video: String? = null,
    var school_name: String? = null,
    var u_dice_code: String? = null,
    var visit_id: String? = null,
    val no_of_teachers_trained: VisitDetails? = null,
    val picture_of_school_name: VisitDetails? = null,
    val selfie_with_school_name_or_u_dice_code: VisitDetails? = null,
    val picture_of_acknowledgement_letter: VisitDetails? = null,
    val number_of_students_as_per_record: VisitDetails? = null,
    val number_of_books_distributed: VisitDetails? = null,
    val no_of_filled_trackers_collected: VisitDetails? = null,
    val school_closed: VisitDetails? = null,
    val school_representative_who_collected_the_books: VisitDetails? = null,
    val name_of_the_school_representative_who_collected_the_books: VisitDetails? = null,
    val mobile_number_of_the_school_representative_who_collected_the_books: VisitDetails? = null,
    val curriculum_on_track: VisitDetails? = null,
    val name_of_the_principal: VisitDetails? = null,
    val mobile_number_of_the_principal: VisitDetails? = null,
    val revisit_applicable: VisitDetails? = null,
    val principal_contact_details: VisitDetails? = null,
    val principal: VisitDetails? = null,
    val visit_image_1: VisitDetails? = null,
    val visit_image_2: VisitDetails? = null,
    val visit_image_3: VisitDetails? = null,
    val visit_image_4: VisitDetails? = null,
    val remark: VisitDetails? = null,
    val revisit: VisitDetails? = null,

)
