package com.hul.api

import com.hul.data.*
import com.hul.data.RequestModel
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Created by Nitin Chorge on 23-11-2020.
 */
interface ApiInterface {

    @GET("app/projects/v1/locations/")
    fun getLocationList(@Query("projectId") projectId: String?): Call<ResponseBody?>

    @GET("app/projects/v1/locations/")
    fun getSchoolCodes(@Query("projectId") projectId: String?,@Query("externalId") externalId: String?): Call<ResponseBody?>

    @GET("app/users/v1/get_user_attendance/")
    fun getAttendence(): Call<ResponseBody?>

    @GET("app/users/v1/get_attendance_form_fields/")
    fun getAttendenceForm(@Query("projectId") projectId: String?): Call<ResponseBody?>

    @GET("app/visits/v1/get_list_of_visits/")
    fun getVisitList() : Call<ResponseBody?>

    @GET("app/visits/v1/get_list_of_visits/")
    fun getVisitListSingle(@Query("locationId ") locationId : String?) : Call<ResponseBody?>

    @GET("app/leads/v1/getLeads/{lead_id}")
    fun getLeadDetails(@Path(value = "lead_id", encoded = true) leadId : String): Call<ResponseBody?>

    @GET("s3upload/v1/getUploadedImagesList/{lead_id}")
    fun getUploadedDocument(@Path(value = "lead_id", encoded = true) leadId : String): Call<ResponseBody?>

    @PUT("app/leads/v1/submitInspection/{lead_id}")
    fun submitLead(@Path(value = "lead_id", encoded = true) leadId : String, @Body param:RequestModel): Call<ResponseBody?>

    @PUT("app/leads/v1/verifyLeadDetails/{lead_id}")
    fun confirmLead(@Path(value = "lead_id", encoded = true) leadId : String, @Query("isVerified") isVerified: Boolean,
                    @Query("remarks") remarks: String, @Body param: RequestModel
    ): Call<ResponseBody?>

    @POST("app/users/v1/verify_otp")
    fun loginUser(@Body param: RequestModel?): Call<ResponseBody?>

    @GET("app/projects/v1/logo/{projectId}")
    fun getLogo(@Path(value = "projectId", encoded = true) projectId : String,): Call<ResponseBody?>

    @GET("app/projects/v1/bannerImage/{projectId}")
    fun getBannerImage(@Path(value = "projectId", encoded = true) projectId : String,): Call<ResponseBody?>

    @POST("app/users/v1/mark_attendance/")
    fun markAttendence(@Body param: RequestModel,@Query("project") project: String,@Query("location_id") location_id: String): Call<ResponseBody?>

    @POST("app/users/v1/send_otp")
    fun sendOTP(@Body param: RequestModel?): Call<ResponseBody?>


    @Multipart
    @POST("app/users/v1/uploadImage/")
    fun upload(
        @Part file: MultipartBody.Part?,
        @Query("projectName") projectName : String?,
    ): Call<ResponseBody?>

    companion object {
        const val BASE_URL = "http://3.7.149.234:8000/"
    }
}
