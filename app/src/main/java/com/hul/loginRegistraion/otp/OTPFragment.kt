package com.hul.loginRegistraion.otp

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.provider.Settings.Secure
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.hul.HULApplication
import com.hul.R
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.controller.APIController
import com.hul.dashboard.Dashboard
import com.hul.data.RequestModel
import com.hul.data.ResponseModel
import com.hul.databinding.FragmentOTPBinding
import com.hul.loginRegistraion.LoginRegisterComponent
import com.hul.loginRegistraion.LoginRegistrationInterface
import com.hul.salg.SalgDashboard
import com.hul.sb.mobiliser.SBMobiliserDashboard
import com.hul.sb.supervisor.SBSupervisorDashboard
import com.hul.screens.field_auditor_dashboard.FieldAuditorDashboard
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.UserTypes
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.nonredirectionAlertDialogue
import com.hul.utils.redirectionAlertDialogue
import com.hul.utils.setProgressDialog
import com.hul.web_form.WebForm
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject


class OTPFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentOTPBinding? = null

    private lateinit var loginRegisterComponent: LoginRegisterComponent

    @Inject
    lateinit var otpViewModel: OTPViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    lateinit var loginRegistrationInterface: LoginRegistrationInterface

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    companion object {
        private const val PERMISSION_REQUEST_CODE = 1
        private val REQUEST_PHONE_STATE_PERMISSION = 101

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOTPBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        loginRegisterComponent =
            (activity?.application as HULApplication).appComponent.loginRegisterComponent()
                .create()
        loginRegisterComponent.inject(this)
        binding.viewModel = otpViewModel

        getLogo()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val regex = """^(?:\D*\d){6}""".toRegex()
        otpViewModel.loginId.value = requireArguments().getString("loginId")
        otpViewModel.encodedLoginId.value =
            otpViewModel.loginId.value!!.replace(regex) { it.value.replace(Regex("""\d"""), "*") }
        binding.numberSubInfo.text =
            requireContext().getString(R.string.otp_sub_info1) + " " + otpViewModel.encodedLoginId.value + " " + requireContext().getString(
                R.string.otp_sub_info2
            )

        binding.loginButton.setOnClickListener {
            if (checkPermission()) {
                loginUser()
            } else {
                requestPermission()
            }

        }

        binding.pinview.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND) {
                // Handle Done or Send action key press
                // Do something
                if (otpViewModel.loginEnabled.value!! && !otpViewModel.termsAccepted.value!!) {
                    if (checkPermission()) {
                        loginUser()
                    } else {
                        requestPermission()
                    }
                }
                return@OnEditorActionListener true
            }
            false
        })
    }

    fun getLogo() {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            //setProgressDialog(requireContext(), "Sending OTP")
            apiController.getApiResponse(
                this,
                RequestModel(projectId = userInfo.projectId),
                ApiExtentions.ApiDef.GET_LOGO.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.GET_LOGO.ordinal, this)
        }

    }

    fun getBanner() {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            //setProgressDialog(requireContext(), "Sending OTP")
            apiController.getApiResponse(
                this,
                RequestModel(projectId = userInfo.projectId),
                ApiExtentions.ApiDef.GET_BANNER.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.GET_BANNER.ordinal, this)
        }

    }

    fun loginUser() {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            setProgressDialog(requireContext(), "Sending OTP")
            apiController.getApiResponse(
                this,
                loginModel(),
                ApiExtentions.ApiDef.LOGIN.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.LOGIN.ordinal, this)
        }

    }

    private fun loginModel(): RequestModel {
        return RequestModel(
            mobile = "+91" + otpViewModel.loginId.value,
            otp = otpViewModel.otp.value,
            app_version = 6
        )
    }

    private fun deviceModel(): RequestModel {
        val deviceInfo = getDeviceDetails()
        return RequestModel(
            device_id = deviceInfo.device_id,
            make = deviceInfo.make,
            model = deviceInfo.model,
            os = deviceInfo.os
        )
    }

    private fun deviceInfo() {
        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            setProgressDialog(requireContext(), "Getting Device Info")
            apiController.getApiResponse(
                this,
                deviceModel(),
                ApiExtentions.ApiDef.ADD_DEVICE_INFO.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.ADD_DEVICE_INFO.ordinal, this)
        }

    }

    private fun redirectToDashboard() {

        if (userInfo.projectId == "1") {
            when (userInfo.userType) {
                UserTypes.MOBILISER -> {
                    val intent = Intent(activity, Dashboard::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }

                UserTypes.FIELD_AUDITOR -> {
                    val intent = Intent(activity, FieldAuditorDashboard::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }

                else -> {
                    // Handle other cases or default behavior
                }
            }
        } else if (userInfo.projectId == "2") {
            when (userInfo.userType) {
                UserTypes.MOBILISER -> {
                    val intent = Intent(activity, SBMobiliserDashboard::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }

                UserTypes.FIELD_AUDITOR -> {
                    val intent = Intent(activity, SBSupervisorDashboard::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                }

                else -> {
                    // Handle other cases or default behavior
                }
            }
        } else if (userInfo.projectId == "3") {

            val intent = Intent(activity, SBMobiliserDashboard::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)

        }

    }


    private fun getDeviceDetails(): DeviceInfoModel {
        val deviceId = getDeviceId()
        val make = Build.MANUFACTURER
        val model = Build.MODEL
        val os = "Android ${Build.VERSION.RELEASE}"

        val deviceDetails = JSONObject().apply {
            put("device_id", deviceId)
            put("make", make)
            put("model", model)
            put("os", os)
        }
        Log.d("DeviceDetails", deviceDetails.toString())

        return DeviceInfoModel(
            device_id = deviceId ?: "",
            make = make,
            model = model,
            os = os
        )
    }


    private fun getDeviceId(): String? {
        val android_id = Secure.getString(
            requireContext().contentResolver,
            Secure.ANDROID_ID
        )
        return android_id

    }


    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_PHONE_STATE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        requestPermissions(
            arrayOf(Manifest.permission.READ_PHONE_STATE),
            REQUEST_PHONE_STATE_PERMISSION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PHONE_STATE_PERMISSION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loginUser() // Permission granted, proceed with login action
            } else {
                requestPermission()
            }
        }
    }

    override fun onApiSuccess(o: String?, objectType: Int) {

        cancelProgressDialog()
        when (ApiExtentions.ApiDef.values()[objectType]) {
            ApiExtentions.ApiDef.ADD_DEVICE_INFO -> {
                val model: ResponseModel = Gson().fromJson(o, ResponseModel::class.java)
                if (!model.error) {
                    redirectToDashboard()
                }
            }

            ApiExtentions.ApiDef.LOGIN -> {
                val model: ResponseModel = Gson().fromJson(o, ResponseModel::class.java)
                if (!model.error) {
                    userInfo.authToken =
                        model.data!!.get("auth_token").toString() // for active session
                    userInfo.loginId =
                        model.data!!.get("mobile_number").toString().replace("+91", "")
                    userInfo.projectId = model.data!!.get("project_id").toString()
                    userInfo.projectName = model.data!!.get("project_name").toString()
                    userInfo.userType = model.data?.get("user_type").toString()

                    val array = JSONArray(model.data!!.get("areas_mapped").toString())
                    if (array.length() > 0) {
                        userInfo.myArea = array.getJSONObject(0).getString("area_name")
                        userInfo.areaId = array.getJSONObject(0).getString("area_id")
                    }
                    if (ContextCompat.checkSelfPermission(
                            requireContext(),
                            Manifest.permission.READ_PHONE_STATE
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            requireActivity(),
                            arrayOf(Manifest.permission.READ_PHONE_STATE),
                            PERMISSION_REQUEST_CODE
                        )
                    } else {
                        deviceInfo()
                    }
                } else {
                    redirectionAlertDialogue(requireContext(), model.message!!)
                }

            }

            ApiExtentions.ApiDef.GET_LOGO -> {
                val model: ResponseModel = Gson().fromJson(o, ResponseModel::class.java)
                if (!model.error) {
                    getBanner()
                    val imageBytes =
                        Base64.decode(model.data!!.get("logo").toString(), Base64.DEFAULT)
                    val decodedImage =
                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.logoimage.setImageBitmap(decodedImage)
                } else {
                    redirectionAlertDialogue(requireContext(), model.message!!)
                }

            }

            ApiExtentions.ApiDef.GET_BANNER -> {
                val model: ResponseModel = Gson().fromJson(o, ResponseModel::class.java)
                if (!model.error) {
                    val imageBytes = Base64.decode(
                        model.data!!.get("project_image").toString(),
                        Base64.DEFAULT
                    )
                    val decodedImage =
                        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.preview.setImageBitmap(decodedImage)
                } else {
                    redirectionAlertDialogue(requireContext(), model.message!!)
                }

            }

            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onApiError(message: String?) {
        cancelProgressDialog()
        nonredirectionAlertDialogue(requireContext(), message!!)
    }

    override fun retry(type: Int) {

        when (ApiExtentions.ApiDef.values()[type]) {
            ApiExtentions.ApiDef.LOGIN -> loginUser()
            ApiExtentions.ApiDef.ADD_DEVICE_INFO -> deviceInfo()
            ApiExtentions.ApiDef.GET_LOGO -> getLogo()
            ApiExtentions.ApiDef.GET_BANNER -> getBanner()
            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG)
                .show()
        }

    }
}