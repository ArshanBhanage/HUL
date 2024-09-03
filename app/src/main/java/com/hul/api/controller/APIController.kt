package com.hul.api.controller

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.hul.R
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.ApiInterface
import com.hul.data.RequestModel
import com.hul.storage.SharedPreferencesStorage
import com.hul.storage.Storage
import com.hul.user.UserInfo
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import javax.inject.Inject

/**
 * Created by Nitin Chorge on 03-12-2020.
 */
class APIController @Inject constructor(private val mContext: Context) : Callback<ResponseBody?> {

    @Inject
    lateinit var retrofit: Retrofit

    private var apiId: Int = 0

    private lateinit var mHandler: ApiHandler

    fun getApiResponse(handler: ApiHandler?, requestModel: RequestModel?, type: Int) {
        apiId = type
        this.mHandler = handler!!

        when (ApiExtentions.ApiDef.entries[apiId]) {
            ApiExtentions.ApiDef.SOCIETY_VISIT_LIST -> retrofit.create(ApiInterface::class.java)
                .getSocietyList(requestModel).enqueue(this)

            ApiExtentions.ApiDef.LIST_OF_CODES -> retrofit.create(ApiInterface::class.java)
                .getCodeList(requestModel!!.areaId!!.toDouble().toInt(), true).enqueue(this)

            ApiExtentions.ApiDef.ADD_VISIT_SOCIETY -> retrofit.create(ApiInterface::class.java)
                .addVisit(requestModel).enqueue(this)

            ApiExtentions.ApiDef.SEND_OTP -> retrofit.create(ApiInterface::class.java)
                .sendOTP(requestModel).enqueue(this)

            ApiExtentions.ApiDef.LOGIN -> retrofit.create(ApiInterface::class.java)
                .loginUser(requestModel).enqueue(this)

            ApiExtentions.ApiDef.ADD_DEVICE_INFO -> retrofit.create(ApiInterface::class.java)
                .deviceInfo(requestModel).enqueue(this)

            ApiExtentions.ApiDef.ADD_VISIT -> retrofit.create(ApiInterface::class.java)
                .addVisit(requestModel).enqueue(this)

            ApiExtentions.ApiDef.GET_LOGO -> retrofit.create(ApiInterface::class.java)
                .getLogo(requestModel!!.projectId!!).enqueue(this)

            ApiExtentions.ApiDef.GET_BANNER -> retrofit.create(ApiInterface::class.java)
                .getBannerImage(requestModel!!.projectId!!).enqueue(this)

            ApiExtentions.ApiDef.GET_VISIT_FORM -> retrofit.create(ApiInterface::class.java)
                .getVisitFormFields(requestModel!!.projectId!!, requestModel!!.visit_number!!)
                .enqueue(this)

            ApiExtentions.ApiDef.LOCATION_LIST -> retrofit.create(ApiInterface::class.java)
                .getLocationList(requestModel!!.projectId).enqueue(this)

            ApiExtentions.ApiDef.SCHOOL_CODES -> retrofit.create(ApiInterface::class.java)
                .getSchoolCodes(requestModel!!.projectId, requestModel.externalId).enqueue(this)

            ApiExtentions.ApiDef.MARK_ATTENDENCE -> retrofit.create(ApiInterface::class.java)
                .markAttendence(requestModel!!, requestModel.project!!, requestModel.location_id!!)
                .enqueue(this)

            ApiExtentions.ApiDef.GET_ATTENDENCE -> retrofit.create(ApiInterface::class.java)
                .getAttendence().enqueue(this)

            ApiExtentions.ApiDef.ATTENDENCE_FORM -> retrofit.create(ApiInterface::class.java)
                .getAttendenceForm(requestModel!!.projectId).enqueue(this)

            ApiExtentions.ApiDef.VISIT_LIST -> retrofit.create(ApiInterface::class.java)
                .getVisitList().enqueue(this)

            ApiExtentions.ApiDef.VISIT_LIST_BY_STATUS -> retrofit.create(ApiInterface::class.java)
                .getVisitListByStatus(requestModel!!.status).enqueue(this)

            ApiExtentions.ApiDef.VISIT_LIST_BY_STATUS2 -> retrofit.create(ApiInterface::class.java)
                .getVisitListByStatus2(
                    requestModel!!.status,
                     requestModel.userType
                ).enqueue(this)

            ApiExtentions.ApiDef.VISIT_LIST_SINGLE -> retrofit.create(ApiInterface::class.java)
                .getVisitListSingle(requestModel!!.location_id).enqueue(this)

            ApiExtentions.ApiDef.VISIT_LIST_FIELD_AUDITOR -> if (requestModel != null) {
                requestModel.mobiliserId?.let {
                    retrofit.create(ApiInterface::class.java)
                        .getVisitListSingle(requestModel.userType, it).enqueue(this)
                }
            }

            ApiExtentions.ApiDef.VISIT_LIST_BY_SCHOOL_CODE -> requestModel?.schoolId?.let {
                retrofit.create(ApiInterface::class.java).getListOfVisits(it).enqueue(this)
            }

            ApiExtentions.ApiDef.VISIT_LIST_BY_ID -> requestModel?.visit_id?.let {
                retrofit.create(ApiInterface::class.java).getListOfVisits(it.toInt()).enqueue(this)
            }

            ApiExtentions.ApiDef.VISIT_LIST_BY_SCHOOL_CODE_Completed -> requestModel?.schoolId?.let {
                retrofit.create(ApiInterface::class.java).getListOfVisits(it).enqueue(this)
            }


            ApiExtentions.ApiDef.LEAD_DETAILS -> retrofit.create(ApiInterface::class.java)
                .getLeadDetails(requestModel!!.leadId!!).enqueue(this)

            ApiExtentions.ApiDef.SUBMIT_LEAD -> retrofit.create(ApiInterface::class.java)
                .submitLead(requestModel!!.leadId!!, RequestModel()).enqueue(this)

            ApiExtentions.ApiDef.UPLOADED_DOCUMENT_LIST -> retrofit.create(ApiInterface::class.java)
                .getUploadedDocument(requestModel!!.leadId!!).enqueue(this)

            ApiExtentions.ApiDef.GET_USER_DETAILS -> retrofit.create(ApiInterface::class.java)
                .getUserDetails().enqueue(this)

            ApiExtentions.ApiDef.GET_PERFORMANCE -> retrofit.create(ApiInterface::class.java)
                .getPerformance(requestModel!!.date_filter).enqueue(this)

            ApiExtentions.ApiDef.GET_VISIT_DATA -> if (requestModel != null) {
                retrofit.create(ApiInterface::class.java)
                    .getVisitData(
                        requestModel.visitId!!,
                        requestModel.project,
                        requestModel.collected_by,
                        requestModel.loadImages
                    )
                    .enqueue(this)
            }

            ApiExtentions.ApiDef.GET_VISIT_DATA_IMAGE -> if (requestModel != null) {
                retrofit.create(ApiInterface::class.java)
                    .getVisitData(
                        requestModel.visitId!!,
                        requestModel.project,
                        requestModel.collected_by,
                        requestModel.loadImages
                    )
                    .enqueue(this)
            }

            ApiExtentions.ApiDef.VISIT_DATA -> retrofit.create(ApiInterface::class.java)
                .visitData(requestModel!!).enqueue(this)

            ApiExtentions.ApiDef.SUBMIT_SCHOOL_FORM -> {
                val json = requestModel!!.form2Data!!.toString()

                // Create RequestBody from JSON string
                val requestBody =
                    json.toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                val map: Map<String, Any> = requestModel!!.form2Data!!.toMap()
                retrofit.create(ApiInterface::class.java)
                    .submitForm(requestBody).enqueue(this)
            }

            ApiExtentions.ApiDef.SAVE_SCHOOL_ACTIVITY_DATA -> retrofit.create(ApiInterface::class.java)
                .visitData(requestModel!!).enqueue(this)

            ApiExtentions.ApiDef.GET_DISTRICTS -> requestModel?.projectId?.let {
                retrofit.create(ApiInterface::class.java).getDistricts(it).enqueue(this)
            }

            ApiExtentions.ApiDef.GET_STATES -> requestModel?.projectId?.let {
                retrofit.create(ApiInterface::class.java).getStates(it).enqueue(this)
            }

            ApiExtentions.ApiDef.ADD_NEW_SCHOOL -> retrofit.create(ApiInterface::class.java)
                .addSchool(requestModel!!).enqueue(this)

            else -> Toast.makeText(mContext, "API NOT INTEGRATED", Toast.LENGTH_LONG).show()
        }

    }

    override fun onResponse(
        call: Call<ResponseBody?>, response: Response<ResponseBody?>
    ) {
        if (response.code() == 200 || response.code() == 201) {
            mHandler.onApiSuccess(response.body()!!.string(), apiId)
        } else if (response.code() == 401) {
            val prefs: SharedPreferencesStorage = SharedPreferencesStorage(mContext)
            val userInfo = UserInfo(storage = prefs)
            userInfo.authToken = ""
            if (ApiExtentions.ApiDef.entries.toTypedArray()[apiId] != ApiExtentions.ApiDef.GET_LOGO && ApiExtentions.ApiDef.values()[apiId] != ApiExtentions.ApiDef.GET_BANNER) {
                mHandler.onApiError(mContext.getString(R.string.session_expire))
            }
        } else {

            var response = JSONObject(response.errorBody()!!.string())
            try {
                mHandler.onApiError(response.getJSONArray("error_details").get(0).toString())
            } catch (e: Exception) {
                if (ApiExtentions.ApiDef.entries.toTypedArray()[apiId] != ApiExtentions.ApiDef.GET_LOGO && ApiExtentions.ApiDef.values()[apiId] != ApiExtentions.ApiDef.GET_BANNER) {
                    if (ApiExtentions.ApiDef.entries.toTypedArray()[apiId] == ApiExtentions.ApiDef.LOGIN) {
                        mHandler.onApiError(response.getString("message"))
                    } else {
                        mHandler.onApiError("Something went wrong")
                    }
                }
            }

//            if (ApiExtentions.ApiDef.values()[apiId] == ApiExtentions.ApiDef.UPI_MANDATE_STATUS)
//                displayMessage(mContext, "Mandate Not Approved.")

        }
    }

    override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
        Log.i(APIController::class.qualifiedName, "Error : " + t.localizedMessage)
        t.localizedMessage?.let { Log.d("Errororo", it) }
        if (t.localizedMessage != null) {
            if (t.localizedMessage.contains("3.7.149.234") || t.localizedMessage.contains("api.goodmetrics.in")) {
                mHandler.onApiError("Can not establish connection.\nCheck your Internet Connectivity.")
            } else {
                mHandler.onApiError("Something went wrong")
                //FirebaseCrash.report(t);
            }
        } else {
            mHandler.onApiError("Can not establish connection.\nCheck your Internet Connectivity.")
        }
        Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show()
    }

    fun JSONObject.toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = this.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = this.get(key)
        }
        return map
    }
}