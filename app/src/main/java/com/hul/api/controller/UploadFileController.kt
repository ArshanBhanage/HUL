package com.hul.api.controller

import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.hul.api.ApiHandler
import com.hul.api.ApiInterface
import com.hul.data.*
import com.hul.utils.FileUtils.getFile
import com.hul.data.RequestModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.File
import javax.inject.Inject

/**
 * Created by Nitin Chorge on 08-01-2021.
 */
class UploadFileController @Inject constructor(private val mContext: Context) :
    Callback<ResponseBody?> {

    @Inject
    lateinit var retrofit: Retrofit

    private var apiId: Int = 0

    private lateinit var mHandler: ApiHandler

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    fun getApiResponse(
        handler: ApiHandler?,
        fileUri: Uri,
        requestModel: RequestModel?,
        type: Int
    ) {
        apiId = type
        val file: File? = if (fileUri.toString().contains("content:") || fileUri.toString()
                .contains("file:")
        ) getFile(mContext, fileUri) else File(fileUri.toString())
        val requestFile = file!!.asRequestBody("multipart/form-data".toMediaTypeOrNull())

        // MultipartBody.Part is used to send also the actual file name
        val body: MultipartBody.Part =
            MultipartBody.Part.createFormData("file", file.name, requestFile)
        this.mHandler = handler!!
        if (requestModel != null) {
            retrofit.create(ApiInterface::class.java)
                .upload(body,requestModel.project, requestModel.uploadFor, requestModel.filename)
                .enqueue(this)
        }


    }

    override fun onResponse(
        call: Call<ResponseBody?>,
        response: Response<ResponseBody?>
    ) {
        if (response.code() == 200 || response.code() == 201) {
            mHandler.onApiSuccess(response.body()!!.string(), apiId)
        }
    }

    override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
        if (t.localizedMessage != null) {
            Log.d("Erroro == ", t.localizedMessage)
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