package com.hul.dashboard.ui.attendence

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.hul.HULApplication
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.controller.APIController
import com.hul.api.controller.UploadFileController
import com.hul.camera.CameraActivity
import com.hul.curriculam.Curriculam
import com.hul.dashboard.Dashboard
import com.hul.dashboard.DashboardComponent
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.data.UploadImageData
import com.hul.databinding.FragmentAttendenceBinding
import com.hul.sb.mobiliser.SBMobiliserDashboard
import com.hul.screens.field_auditor_dashboard.FieldAuditorDashboard
import com.hul.screens.field_auditor_dashboard.ui.image_preview.ImagePreviewDialogFragment
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.UserTypes
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import com.hul.utils.setProgressDialog
import org.json.JSONObject
import javax.inject.Inject


class AttendenceFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentAttendenceBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var dashboardComponent: DashboardComponent

    @Inject
    lateinit var uploadFileController: UploadFileController

    @Inject
    lateinit var attendenceViewModel: AttendenceViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

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

    var imageIndex: Int = 0

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all {
                it.value == true
            }
            if (granted) {
                checkLocationSettings()
            } else {
                showInformationMessage()
            }
        }

    private fun showInformationMessage() {
        AlertDialog.Builder(requireActivity())
            .setTitle("Permissions Needed")
            .setMessage("You have denied the permissions. Please go to settings and allow the permissions manually.")
            .setPositiveButton("Settings") { dialog, _ ->
                requestPermissionSettings()
                dialog.dismiss() // This dismisses the dialog
            }
            .setCancelable(false)
            .show()
    }

    private fun requestPermissionSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireActivity().packageName, null)
        }
        requestPermissionSetting.launch(intent)
    }


    private val requestPermissionSetting =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { permissions ->
            if(!allPermissionsGranted()) {
                showInformationMessage()
            }
        }


    private val requestLocation =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { permissions ->
            checkLocationSettings()
        }

    private fun checkLocationSettings() {
        val locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager
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

    fun requestPermission() {
        requestPermission.launch(REQUIRED_PERMISSIONS)
    }

    fun requestLocation() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        requestLocation.launch(intent)
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {

        // Initialize FusedLocationProviderClient
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())


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

                attendenceViewModel.longitude.value = currentLocation!!.longitude.toString()
                attendenceViewModel.lattitude.value = currentLocation!!.latitude.toString()
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAttendenceBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        dashboardComponent =
            (activity?.application as HULApplication).appComponent.dashboardComponent()
                .create()
        dashboardComponent.inject(this)
        binding.viewModel = attendenceViewModel

        binding.selfieCapture.setOnClickListener {
            redirectToCamera(0, attendenceViewModel.imageCaptureType1.value!!, "Selfie at first school")
        }

        binding.selfieCapture3.setOnClickListener {
            redirectToCamera(0, attendenceViewModel.imageType1.value!!, "Selfie at the location")
        }

        binding.curriculumCapture.setOnClickListener {
            redirectToCamera(1, attendenceViewModel.imageCaptureType2.value!!, "Full image of mobiliser with Curriculum material")
        }

        binding.stats.setOnClickListener {
            requireActivity().onBackPressed()
        }

//        binding.retake1.setOnClickListener {
//            redirectToCamera(0,attendenceViewModel.imageType1.value!!)
//        }
//
//        binding.retake2.setOnClickListener {
//            redirectToCamera(1,attendenceViewModel.imageType2.value!!)
//        }

        binding.markAttendence.setOnClickListener {
            attendenceViewModel.position.value = 1
            if (imageIndex == 0) {
                setProgressDialog(requireContext(), "Uploading")
                uploadImage(attendenceViewModel.imageUrl1.value?.toUri()!!)
            }
        }

        binding.markAttendence2.setOnClickListener {
            if(attendenceViewModel.imageUrl1.value != null && attendenceViewModel.imageUrl1.value!!.length > 0) {
                attendenceViewModel.position.value = 1
                uploadImage(attendenceViewModel.imageUrl1.value?.toUri()!!)
            }
        }

        if (allPermissionsGranted()) {
            checkLocationSettings()
        } else {
            requestPermission()
        }

        attendenceViewModel.projectInfo.value =
            Gson().fromJson(requireArguments().getString("projectInfo"), ProjectInfo::class.java)

        binding.view1.setOnClickListener { attendenceViewModel.imageUrl1.value?.let { it1 ->
            showImagePreview(
                it1
            )
        } }
        binding.view2.setOnClickListener { attendenceViewModel.imageUrl1.value?.let { it1 ->
            showImagePreview(
                it1
            )
        } }
        binding.view2.setOnClickListener { attendenceViewModel.imageUrl2.value?.let { it1 ->
            showImagePreview(
                it1
            )
        } }

        if(userInfo.projectId == "1") {
            getAttendenceForm()
            binding.card1.visibility = View.VISIBLE
            binding.card2.visibility = View.GONE
        }
        else{
            binding.card1.visibility = View.GONE
            binding.card2.visibility = View.VISIBLE
        }

        return root
    }

    private fun showImagePreview(imagePath: String) {
        val imageUri = Uri.parse(imagePath)
        val newFragment = ImagePreviewDialogFragment.newInstance(imageUri)
        newFragment.show(childFragmentManager, "image_preview")
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    fun markAttendence2() {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            setProgressDialog(requireContext(), "Loading Leads")
            apiController.getApiResponse(
                this,
                markAttendenceModel(),
                ApiExtentions.ApiDef.MARK_ATTENDENCE.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.MARK_ATTENDENCE.ordinal, this)
        }

    }

    private fun markAttendenceModel2(): RequestModel {
        return RequestModel(
            project = userInfo.projectName,
            location_id = attendenceViewModel.projectInfo.value!!.location_id,
            lattitude = attendenceViewModel.lattitude.value,
            longitude = attendenceViewModel.longitude.value,
            photo_url1 = attendenceViewModel.imageUrl1API.value,
            photo_url1_description = binding.image3Description.text.toString(),
        )
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()


    }

    fun markAttendence() {
        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            setProgressDialog(requireContext(), "Loading Leads")
            apiController.getApiResponse(
                this,
                markAttendenceModel(),
                ApiExtentions.ApiDef.MARK_ATTENDENCE.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.MARK_ATTENDENCE.ordinal, this)
        }

    }

    private fun markAttendenceModel(): RequestModel {
        return RequestModel(
            project = userInfo.projectName,
            location_id = attendenceViewModel.projectInfo.value!!.location_id,
            lattitude = attendenceViewModel.lattitude.value,
            longitude = attendenceViewModel.longitude.value,
            photo_url1 = attendenceViewModel.imageUrl1API.value,
            photo_url2 = attendenceViewModel.imageUrl2API.value,
            photo_url1_description = binding.image1Description.text.toString(),
            photo_url2_description = binding.image2Description.text.toString()
        )
    }

    fun getAttendenceForm() {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            setProgressDialog(requireContext(), "Loading Leads")
            apiController.getApiResponse(
                this,
                getAttendenceFormModel(),
                ApiExtentions.ApiDef.ATTENDENCE_FORM.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.ATTENDENCE_FORM.ordinal, this)
        }

    }

    private fun getAttendenceFormModel(): RequestModel {
        return RequestModel(
            projectId = userInfo.projectId,
        )
    }

    private fun uploadImage(imageUri: Uri) {
        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            uploadFileController.getApiResponse(
                this,
                imageUri,
                uploadImageModel(),
                ApiExtentions.ApiDef.UPLOAD_IMAGE.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.UPLOAD_IMAGE.ordinal, this)
        }
    }

    private fun uploadImageModel(): RequestModel {
        var fileName: String = ""
        val visitPrefix = "project_" + userInfo.projectName;
        if (imageIndex == 0) {
            fileName = visitPrefix + "_"+getTag(binding.image1Description.text.toString())+".jpeg";
        }else if (imageIndex == 1) {
            fileName = visitPrefix + "_"+getTag(binding.image2Description.text.toString())+".jpeg";
        }
        return RequestModel(
            project = userInfo.projectName,
            uploadFor = "attendance",
            filename = fileName,
        )
    }

    private fun getTag(string : String) : String
    {
        return string.replace(" ", "_").lowercase()
    }

    private fun deleteImage(uri: Uri) {
        try {
            val resolver = requireActivity().contentResolver
            val rowsDeleted = resolver.delete(uri, null, null)
            if (rowsDeleted > 0) {
                // File successfully deleted
            } else {
                // File not found or couldn't be deleted
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle the error
        }
    }

    override fun onApiSuccess(o: String?, objectType: Int) {

        cancelProgressDialog()
        when (ApiExtentions.ApiDef.entries[objectType]) {

            ApiExtentions.ApiDef.ATTENDENCE_FORM -> {
                val model = JSONObject(o.toString())
                binding.image1Description.text =
                    model.getJSONObject("data").getJSONArray("form_fields").getJSONObject(0)
                        .getString("form_field_title")
                binding.image2Description.text =
                    model.getJSONObject("data").getJSONArray("form_fields").getJSONObject(1)
                        .getString("form_field_title")

                attendenceViewModel.imageType1.value =
                    model.getJSONObject("data").getJSONArray("form_fields").getJSONObject(0)
                        .getString("input_type")
                attendenceViewModel.imageType2.value =
                    model.getJSONObject("data").getJSONArray("form_fields").getJSONObject(1)
                        .getString("input_type")

            }

            ApiExtentions.ApiDef.MARK_ATTENDENCE -> {
                val model = JSONObject(o.toString())
                /*if (attendenceViewModel.projectInfo.value!!.project_name != null) {
                    redirectToCurriculam(attendenceViewModel.projectInfo.value!!)
                } else {
                    requireActivity().onBackPressed()
                }*/
                redirectToDashboard()
            }

            ApiExtentions.ApiDef.UPLOAD_IMAGE -> {
                val model = JSONObject(o.toString())
                val uploadImageData = Gson().fromJson(
                    model.getJSONObject("data").toString(),
                    UploadImageData::class.java
                )
                if (uploadImageData != null && imageIndex == 0) {
                    imageIndex += 1;
                    attendenceViewModel.imageUrl1API.value = uploadImageData.url
                    if(userInfo.projectId == "1") {
                        uploadImage(attendenceViewModel.imageUrl2.value?.toUri()!!)
                        deleteImage(attendenceViewModel.imageUrl1.value?.toUri()!!)
                    }
                    else{
                        markAttendence2()
                        deleteImage(attendenceViewModel.imageUrl2.value?.toUri()!!)
                    }

                } else if (uploadImageData != null && imageIndex == 1) {
                    imageIndex += 1;
                    attendenceViewModel.imageUrl2API.value = uploadImageData.url
                    markAttendence()
                }
            }

            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }
    }

    private fun redirectToDashboard() {
        if(userInfo.projectId == "1") {
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
        else{
            when (userInfo.userType) {
                UserTypes.MOBILISER -> {
                    val intent = Intent(activity, SBMobiliserDashboard::class.java)
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

    override fun onApiError(message: String?) {
        redirectionAlertDialogue(requireContext(), message!!)
    }

    override fun retry(type: Int) {

        when (ApiExtentions.ApiDef.entries[type]) {
            ApiExtentions.ApiDef.MARK_ATTENDENCE -> markAttendence()
            ApiExtentions.ApiDef.ATTENDENCE_FORM -> getAttendenceForm()
            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }

    }

    private fun redirectToCamera(position: Int, imageType: String, heading: String) {
        val intent = Intent(activity, CameraActivity::class.java)
        intent.putExtra("position", position)
        if (position == 0) {
            intent.putExtra("imageType", "Image Capture Front")
        }else{
            intent.putExtra("imageType", "Back")
        }
        intent.putExtra("heading", heading)
        startImageCapture.launch(intent)
    }

    private fun redirectToCurriculam(projectInfo: ProjectInfo) {
        val intent = Intent(activity, Curriculam::class.java)
        intent.putExtra("projectInfo", Gson().toJson(projectInfo))
        startActivity(intent)
    }

    //private val startImageCapture = registerForActivityResult(CameraActivity::class.java)

    var startImageCapture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                if (data!!.getIntExtra("position", 0) == 0) {
                    attendenceViewModel.imageUrl1.value = result.data!!.getStringExtra("imageUrl")
                } else {
                    attendenceViewModel.imageUrl2.value = result.data!!.getStringExtra("imageUrl")
                }
            }
        }

    companion object {
        private const val TAG = "CameraXGFG"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 20
        private val REQUIRED_PERMISSIONS =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R){
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                )
            }
            else{
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )

            }
    }

}