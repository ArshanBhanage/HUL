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
import androidx.navigation.ui.AppBarConfiguration
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
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

class WebForm : AppCompatActivity(), ApiHandler, RetryInterface {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityWebFormBinding

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    private lateinit var webFormComponent: WebFormComponent

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    @Inject
    lateinit var uploadFileController: UploadFileController

    // TODO: Step 1.1, Review variables (no changes).
// FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you
// should receive updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

    // Used only for local storage of the last known location. Usually, this would be saved to your
// database, but because this is a simplified sample without a full database, we only need the
// last location to create a Notification if the user navigates away from the app.
    private var currentLocation: Location? = null

    private lateinit var formElementList : ArrayList<FormElement>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityWebFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        webFormComponent = (application as HULApplication).appComponent.webFormComponent().create()
        webFormComponent.inject(this)

        if (allPermissionsGranted()) {
            checkLocationSettings()
        } else {
            requestPermission()
        }

        val allowOnlyLettersAndSpacesFilter =
            InputFilter { source, start, end, dest, dstart, dend ->
                for (i in start until end) {
                    if (!source[i].isLetter() && !source[i].isWhitespace()) {
                        return@InputFilter ""
                    }
                }
                null
            }

        getVisitData()

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkLocationSettings() {
        val locationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isGpsEnabled) {
            //Toast.makeText(this, "GPS is not enabled", Toast.LENGTH_SHORT).show()
            requestLocation()
        } else {
            requestLocationUpdates()
            // Location services are enabled
            //Toast.makeText(this, "Location services are enabled", Toast.LENGTH_SHORT).show()
            // Proceed with location-related operations
        }
    }

    fun requestLocation() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        requestLocation.launch(intent)
    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                checkLocationSettings()
            } else {
                requestPermission()
            }
        }

    private val requestLocation =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { permissions ->
            checkLocationSettings()
        }


    fun requestPermission() {
        requestPermission.launch(REQUIRED_PERMISSIONS)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this)


        locationRequest = LocationRequest.create().apply {
            // Sets the desired interval for active location updates. This interval is inexact. You
            // may not receive updates at all if no location sources are available, or you may
            // receive them less frequently than requested. You may also receive updates more
            // frequently than requested if other applications are requesting location at a more
            // frequent interval.
            //
            // IMPORTANT NOTE: Apps running on Android 8.0 and higher devices (regardless of
            // targetSdkVersion) may receive updates less frequently than this interval when the app
            // is no longer in the foreground.
            interval = 60

            // Sets the fastest rate for active location updates. This interval is exact, and your
            // application will never receive updates more frequently than this value.
            fastestInterval = 30

            // Sets the maximum time when batched location updates are delivered. Updates may be
            // delivered sooner than this interval.
            maxWaitTime = 10

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                // Normally, you want to save a new location to a database. We are simplifying
                // things a bit and just saving it as a local variable, as we only need it again
                // if a Notification is created (when the user navigates away from app).
                currentLocation = locationResult.lastLocation

                //attendenceViewModel.longitude.value = currentLocation!!.longitude.toString()
                //attendenceViewModel.lattitude.value = currentLocation!!.latitude.toString()
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)

            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

//        locationCallback = object : LocationCallback() {
//            //This callback is where we get "streaming" location updates. We can check things like accuracy to determine whether
//            //this latest update should replace our previous estimate.
//            override fun onLocationResult(locationResult: LocationResult) {
//
//                if (locationResult == null) {
//                    Log.d(TAG, "locationResult null")
//                    return
//                }
//                Log.d(TAG, "received " + locationResult.locations.size + " locations")
//                for (loc in locationResult.locations) {
//                    cameraPreviewViewModel.longitude.value = loc.longitude.toString()
//                    cameraPreviewViewModel.lattitude.value = loc.latitude.toString()
//                    if (cameraPreviewViewModel.uri.value != null) {
//                        cancelProgressDialog()
//                        redurectToImagePreview(cameraPreviewViewModel.uri.value!!)
//                    }
//                }
//            }
//
//            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
//                Log.d(TAG, "locationAvailability is " + locationAvailability.isLocationAvailable)
//                super.onLocationAvailability(locationAvailability)
//            }
//        }

//        val locationRequest = LocationRequest.create().apply {
//            interval = 100 // Update interval in milliseconds
//            fastestInterval = 500 // Fastest update interval in milliseconds
//            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
//        }
//
//        fusedLocationClient.requestLocationUpdates(
//            locationRequest,
//            locationCallback,
//            null /* Looper */
//        )
    }

    private fun createForm(formElements: List<FormElement>) {
        for (element in formElements) {
            when (element.input_type) {
                "Textbox" -> inflateEditTextLayout(element)
                "Image Capture" -> inflateCaptureLayout(element)
                "Dropdown" -> inflateSpinnerLayout(element)
            }
        }
    }

    private fun inflateEditTextLayout(element: FormElement) {
        val view = layoutInflater.inflate(R.layout.layout_edittext, binding.formContainer, false)
        view.findViewById<TextView>(R.id.label).text = element.form_field_title
        //view.findViewById<EditText>(R.id.editText).hint = element.placeholder
        binding.formContainer.addView(view)
    }

    private fun inflateRadioButtonLayout(element: FormElement) {
        val view = layoutInflater.inflate(R.layout.layout_radiobutton, binding.formContainer, false)
        view.findViewById<TextView>(R.id.label).text = element.form_field_title
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
//        element.options?.forEach { option ->
//            val radioButton = RadioButton(this).apply {
//                text = option
//            }
//            radioGroup.addView(radioButton)
//        }
        binding.formContainer.addView(view)
    }

    private fun inflateCaptureLayout(element: FormElement) {

        val view = layoutInflater.inflate(R.layout.layout_file_upload, binding.formContainer, false)
        view.findViewById<TextView>(R.id.label).text = element.form_field_title
        view.findViewById<AppCompatButton>(R.id.capture).setOnClickListener {
            redirectToCamera(0, "Back", getString(R.string.school_pic1))
        }
//        val spinner = view.findViewById<Spinner>(R.id.spinner)
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, element.options ?: listOf())
//        spinner.adapter = adapter
        binding.formContainer.addView(view)
    }

    private fun inflateSpinnerLayout(element: FormElement) {
        val view = layoutInflater.inflate(R.layout.layout_dropdown, binding.formContainer, false)
        view.findViewById<TextView>(R.id.label).text = element.form_field_title
        val adapter = ArrayAdapter(this, R.layout.list_popup_window_item, Arrays.asList(element.input_allowed_values))
        view.findViewById<AutoCompleteTextView>(R.id.dropdown).setAdapter(adapter)
//        val spinner = view.findViewById<Spinner>(R.id.spinner)
//        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, element.options ?: listOf())
//        spinner.adapter = adapter
        binding.formContainer.addView(view)
    }

    private fun redirectToCamera(position: Int, imageType: String, heading: String) {
        val intent = Intent(this, CameraActivity::class.java)
        intent.putExtra("position", position)
        intent.putExtra("imageType", imageType)
        intent.putExtra("heading", heading)
        startImageCapture.launch(intent)
    }

    val startImageCapture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val position = data!!.getIntExtra("position", 0)
                val imageUrl = result.data!!.getStringExtra("imageUrl")

//                startTimer()
//
//                // Update the view model's imageUrl at the corresponding position
//                when (position) {
//                    0 -> form1FillViewModel.imageUrl1.value = imageUrl
//                    1 -> form1FillViewModel.imageUrl2.value = imageUrl
//                    2 -> form1FillViewModel.imageUrl3.value = imageUrl
//                    3 -> form1FillViewModel.imageUrl4.value = imageUrl
//                }
            }
        }

    private fun visitsDataModel(): RequestModel {
        return RequestModel(
            projectId = "2",
            visit_number = "1"
        )
    }

    private fun getVisitData() {
        if (ConnectionDetector(this).isConnectingToInternet()) {
            //setProgressDialog(requireContext(), "Loading Visit data")
            apiController.getApiResponse(
                this,
                visitsDataModel(),
                ApiExtentions.ApiDef.GET_VISIT_FORM.ordinal
            )
        } else {
            noInternetDialogue(this, ApiExtentions.ApiDef.GET_VISIT_FORM.ordinal, this)
        }
    }

    override fun onApiSuccess(o: String?, objectType: Int) {
        when (ApiExtentions.ApiDef.entries[objectType]) {

            ApiExtentions.ApiDef.GET_VISIT_FORM -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<FormElement?>?>() {}.type
                    formElementList =
                        Gson().fromJson(model.getJSONObject("data").getJSONArray("visit_tasks").getJSONObject(0).getJSONArray("form_fields").toString(), listType);

                    createForm(formElementList)
                } else {
                    redirectionAlertDialogue(this, model.getString("message"))
                }
            }
            else -> Toast.makeText(this, "Api Not Integrated", Toast.LENGTH_LONG).show()
        }
    }

    override fun onApiError(message: String?) {
        cancelProgressDialog()
        println(message)
        redirectionAlertDialogue(this, message!!)
    }

    override fun retry(type: Int) {

        when (ApiExtentions.ApiDef.entries[type]) {
            else -> Toast.makeText(this, "Api Not Integrated", Toast.LENGTH_LONG).show()
        }

    }

}