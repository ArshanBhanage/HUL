package com.hul.data

/**
 * Created by Nitin Chorge on 26-11-2020.
 */
data class RequestModel(
    var projectId: String? = null,
    var mobile: String? = null,
    var type: String? = null,
    var otp: String? = null,
    var password: String? = null,
    var leadId: String? = null ,
    var regNo : String? = null,
    var docType  : String? = null,
    var docTypeDescription : String? = null,
    var photo_url1 : String? = null,
    var photo_url1_description : String? = null,
    var photo_url2 : String? = null,
    var photo_url2_description : String? = null,
    var photo_url3 : String? = null,
    var photo_url3_description : String? = null,
    var remarks : String? = null,
    var project : String? = null,
    var location_id : String? = null,
    var lattitude : String? = null,
    var longitude : String? = null,
    var externalId : String? = null,
    var visit_id : String? = null,
    var visitData: VisitData? = null,
    val userType: String? = null,
    val mobiliserId: Int = 0
)