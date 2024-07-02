package com.hul.loginRegistraion.loginwithpin

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.hul.HULApplication
import com.hul.R
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.controller.APIController
import com.hul.dashboard.Dashboard
import com.hul.data.RequestModel
import com.hul.data.ResponseModel
import com.hul.databinding.FragmentLoginWithPinBinding
import com.hul.loginRegistraion.LoginRegisterComponent
import com.hul.loginRegistraion.LoginRegistrationInterface
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
import javax.inject.Inject

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginWithPIN : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentLoginWithPinBinding? = null

    private lateinit var loginRegisterComponent: LoginRegisterComponent

    @Inject
    lateinit var loginWithPINViewModel: LoginWithPINViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    lateinit var loginRegistrationInterface: LoginRegistrationInterface

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentLoginWithPinBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        loginRegisterComponent =
            (activity?.application as HULApplication).appComponent.loginRegisterComponent()
                .create()
        loginRegisterComponent.inject(this)
        binding.viewModel = loginWithPINViewModel

        // Check if user already logged-in
        checkUserLoginStatus()

        binding.loginButton.setOnClickListener {
            loginUser()
        }

        binding.mobNo.setOnEditorActionListener(TextView.OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND) {
                // Handle Done or Send action key press
                // Do something
                if(loginWithPINViewModel.loginEnabled.value!! && !loginWithPINViewModel.buttonClicked.value!!)
                {
                    loginUser()
                }
                return@OnEditorActionListener true
            }
            false
        })
        getLogo()
        //getBanner()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loginWithPINViewModel.loginId.value = userInfo.loginId
        //loginWithPINViewModel.pin.value = "Irsindia@1424"

    }

    override fun onResume() {
        super.onResume()
        loginRegistrationInterface = activity as LoginRegistrationInterface


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
                ApiExtentions.ApiDef.SEND_OTP.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.SEND_OTP.ordinal, this)
        }

    }

    private fun loginModel(): RequestModel {
        return RequestModel(
            mobile = "+91"+loginWithPINViewModel.loginId.value,
            type = "login"
        )
    }


    fun redirectToOTP() {
        val bundle = Bundle()
        bundle.putString("loginId", loginWithPINViewModel.loginId.value)
        findNavController().navigate(R.id.action_LoginWithPIN_to_OTPFragment, bundle)
    }

    private fun redirectToDashboard() {
        val intent = Intent(activity, Dashboard::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        requireActivity().finish()
    }

    private fun checkUserLoginStatus() {
        if (!userInfo.authToken.isEmpty()) {
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
        }
    }

    override fun onApiSuccess(o: String?, objectType: Int) {

        cancelProgressDialog()
        loginWithPINViewModel.buttonClicked.value = false
        Log.d("Nitin", o.toString())
        when (ApiExtentions.ApiDef.values()[objectType]) {

            ApiExtentions.ApiDef.SEND_OTP -> {
                val model: ResponseModel = Gson().fromJson(o,ResponseModel::class.java)
                Log.d("", "onApiSuccess: ${model}")
                if (!model.error) {

//                    userInfo.authToken = model.data!!.get("auth_token").toString() // for active session
//                    userInfo.loginId = model.data!!.get("mobile_no").toString()
                    redirectToOTP()
                } else {
//                    redirectionAlertDialogue(requireContext(), model.message!!)
//                    redirectionAlertDialogue(requireContext(), "User does not exist")
                }

            }
            ApiExtentions.ApiDef.GET_LOGO -> {
                val model: ResponseModel = Gson().fromJson(o,ResponseModel::class.java)
                if (!model.error) {
                    getBanner()
                    val imageBytes = Base64.decode(model.data!!.get("logo").toString(), Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.logoimage.setImageBitmap(decodedImage)
                } else {
                    redirectionAlertDialogue(requireContext(), model.message!!)
                }

            }
            ApiExtentions.ApiDef.GET_BANNER -> {
                val model: ResponseModel = Gson().fromJson(o,ResponseModel::class.java)
                if (!model.error) {
                    val imageBytes = Base64.decode(model.data!!.get("project_image").toString(), Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.preview.setImageBitmap(decodedImage)
                } else {
                    redirectionAlertDialogue(requireContext(), model.message!!)
                }

            }

            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }
    }

    override fun onApiError(message: String?) {
        cancelProgressDialog()
        loginWithPINViewModel.buttonClicked.value = false
        nonredirectionAlertDialogue(requireContext(), message!!)
    }

    override fun retry(type: Int) {

        when (ApiExtentions.ApiDef.values()[type]) {
            ApiExtentions.ApiDef.SEND_OTP -> loginUser()
            ApiExtentions.ApiDef.GET_LOGO -> getLogo()
            ApiExtentions.ApiDef.GET_BANNER -> getBanner()
            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }

    }
}

sealed class LoginState
object InValid : LoginState()
object Valid : LoginState()