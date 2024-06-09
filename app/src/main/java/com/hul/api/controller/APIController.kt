package com.hul.api.controller

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.hul.R
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.ApiInterface
import com.hul.data.RequestModel
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
class APIController @Inject constructor(private val mContext: Context) :
    Callback<ResponseBody?> {

    @Inject
    lateinit var retrofit: Retrofit

    private var apiId: Int = 0

    private lateinit var mHandler: ApiHandler

    fun getApiResponse(handler: ApiHandler?, requestModel: RequestModel?, type: Int) {
        apiId = type
        this.mHandler = handler!!

        when (ApiExtentions.ApiDef.values()[apiId]) {
            ApiExtentions.ApiDef.SEND_OTP -> retrofit.create(ApiInterface::class.java)
                .sendOTP(requestModel)
                .enqueue(this)
            ApiExtentions.ApiDef.LOGIN -> retrofit.create(ApiInterface::class.java)
                .loginUser(requestModel)
                .enqueue(this)

            ApiExtentions.ApiDef.GET_LOGO -> retrofit.create(ApiInterface::class.java)
                .getLogo(requestModel!!.projectId!!)
                .enqueue(this)

            ApiExtentions.ApiDef.GET_BANNER -> retrofit.create(ApiInterface::class.java)
                .getBannerImage(requestModel!!.projectId!!)
                .enqueue(this)

            ApiExtentions.ApiDef.LOCATION_LIST -> retrofit.create(ApiInterface::class.java)
                .getLocationList(requestModel!!.projectId)
                .enqueue(this)

            ApiExtentions.ApiDef.SCHOOL_CODES -> retrofit.create(ApiInterface::class.java)
                .getSchoolCodes(requestModel!!.projectId,requestModel.externalId)
                .enqueue(this)

            ApiExtentions.ApiDef.MARK_ATTENDENCE -> retrofit.create(ApiInterface::class.java)
                .markAttendence(requestModel!!, requestModel.project!!, requestModel.location_id!!)
                .enqueue(this)

            ApiExtentions.ApiDef.GET_ATTENDENCE -> retrofit.create(ApiInterface::class.java)
                .getAttendence()
                .enqueue(this)

            ApiExtentions.ApiDef.ATTENDENCE_FORM -> retrofit.create(ApiInterface::class.java)
                .getAttendenceForm(requestModel!!.projectId)
                .enqueue(this)

            ApiExtentions.ApiDef.VISIT_LIST -> retrofit.create(ApiInterface::class.java)
                .getVisitList()
                .enqueue(this)

            ApiExtentions.ApiDef.VISIT_LIST_SINGLE -> retrofit.create(ApiInterface::class.java)
                .getVisitListSingle(requestModel!!.location_id)
                .enqueue(this)

            ApiExtentions.ApiDef.VISIT_LIST_FIELD_AUDITOR -> retrofit.create(ApiInterface::class.java)
                .getVisitListSingle(requestModel!!.userType, requestModel.mobiliserId)
                .enqueue(this)

            ApiExtentions.ApiDef.LEAD_DETAILS -> retrofit.create(ApiInterface::class.java)
                .getLeadDetails(requestModel!!.leadId!!)
                .enqueue(this)

            ApiExtentions.ApiDef.SUBMIT_LEAD -> retrofit.create(ApiInterface::class.java)
                .submitLead(requestModel!!.leadId!!,RequestModel())
                .enqueue(this)

            ApiExtentions.ApiDef.UPLOADED_DOCUMENT_LIST -> retrofit.create(ApiInterface::class.java)
                .getUploadedDocument(requestModel!!.leadId!!)
                .enqueue(this)

            ApiExtentions.ApiDef.GET_USER_DETAILS -> retrofit.create(ApiInterface::class.java)
                .getUserDetails()
                .enqueue(this)

            ApiExtentions.ApiDef.GET_PERFORMANCE -> retrofit.create(ApiInterface::class.java)
                .getPerformance()
                .enqueue(this)

            else -> Toast.makeText(mContext, "API NOT INTEGRATED", Toast.LENGTH_LONG).show()
        }

    }

    override fun onResponse(
        call: Call<ResponseBody?>,
        response: Response<ResponseBody?>
    ) {
        if (response.code() == 200 || response.code() == 201) {
            mHandler.onApiSuccess(response.body()!!.string(), apiId)
        } else if(response.code() == 401) {
            if(ApiExtentions.ApiDef.values()[apiId]!=ApiExtentions.ApiDef.GET_LOGO && ApiExtentions.ApiDef.values()[apiId]!=ApiExtentions.ApiDef.GET_BANNER) {
                mHandler.onApiError(mContext.getString(R.string.session_expire))
            }
        } else {

            var response = JSONObject(response.errorBody()!!.string())
            try {
                mHandler.onApiError(response.getJSONArray("error_details").get(0).toString())
            }
            catch (e:Exception)
            {
                if(ApiExtentions.ApiDef.values()[apiId]!=ApiExtentions.ApiDef.GET_LOGO && ApiExtentions.ApiDef.values()[apiId]!=ApiExtentions.ApiDef.GET_BANNER) {
                    mHandler.onApiError("Something went wrong")
                }
            }

//            if (ApiExtentions.ApiDef.values()[apiId] == ApiExtentions.ApiDef.UPI_MANDATE_STATUS)
//                displayMessage(mContext, "Mandate Not Approved.")

        }
    }

    override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
        Log.i(APIController::class.qualifiedName, "Error : " + t.localizedMessage)
        if (t.localizedMessage != null) {
            if (t.localizedMessage.contains("13.126.28.53") || t.localizedMessage.contains("ws.mintwalk.com")) {
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
}