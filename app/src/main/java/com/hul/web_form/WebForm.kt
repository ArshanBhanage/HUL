package com.hul.web_form

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.text.InputFilter
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hul.HULApplication
import com.hul.R
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.controller.APIController
import com.hul.api.controller.UploadFileController
import com.hul.camera.CameraActivity
import com.hul.curriculam.ui.form1Fill.Form1FillFragment
import com.hul.dashboard.Dashboard
import com.hul.data.FormElement
import com.hul.data.GetVisitDataResponseData
import com.hul.data.RequestModel
import com.hul.data.State
import com.hul.data.UploadImageData
import com.hul.databinding.ActivityCurriculamBinding
import com.hul.databinding.ActivityWebFormBinding
import com.hul.databinding.LayoutDropdownBinding
import com.hul.databinding.LayoutFileUploadBinding
import com.hul.loginRegistraion.LoginRegisterComponent
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.Arrays
import javax.inject.Inject

class WebForm : AppCompatActivity()  {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityWebFormBinding

    private lateinit var webFormComponent: WebFormComponent

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    @Inject
    lateinit var uploadFileController: UploadFileController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        webFormComponent = (application as HULApplication).appComponent.webFormComponent().create()
        webFormComponent.inject(this)


        val bundle = Bundle()
        bundle.putString("projectInfo", intent.getStringExtra("projectInfo"))
        findNavController(R.id.nav_host_fragment_webform).setGraph(R.navigation.dynamic_form_nav_graph)
        findNavController(R.id.nav_host_fragment_webform).navigate(R.id.dynamicFormFragment, bundle)

    }

}