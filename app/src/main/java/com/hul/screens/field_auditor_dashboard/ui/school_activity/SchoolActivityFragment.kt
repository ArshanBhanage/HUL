package com.hul.screens.field_auditor_dashboard.ui.school_activity

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
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.hul.HULApplication
import com.hul.R
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.controller.APIController
import com.hul.api.controller.UploadFileController
import com.hul.camera.CameraActivity
import com.hul.data.GetVisitDataResponseData
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.data.UploadImageData
import com.hul.data.VisitData
import com.hul.data.VisitDetails
import com.hul.databinding.FragmentSchoolActivityBinding
import com.hul.screens.field_auditor_dashboard.FieldAuditorDashboardComponent
import com.hul.screens.field_auditor_dashboard.ui.image_preview.ImagePreviewDialogFragment
import com.hul.storage.SharedPreferencesStorage
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import com.hul.utils.setProgressDialog
import org.json.JSONObject
import javax.inject.Inject


class SchoolActivityFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentSchoolActivityBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var dashboardComponent: FieldAuditorDashboardComponent

    @Inject
    lateinit var uploadFileController: UploadFileController

    @Inject
    lateinit var schoolActivityViewModel: SchoolActivityViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    @Inject
    lateinit var prefs: SharedPreferencesStorage

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

    private fun requestPermission() {
        requestPermission.launch(REQUIRED_PERMISSIONS)
    }

    private fun requestLocation() {
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

                schoolActivityViewModel.longitude.value = currentLocation!!.longitude.toString()
                schoolActivityViewModel.lattitude.value = currentLocation!!.latitude.toString()
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)

            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSchoolActivityBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        dashboardComponent =
            (activity?.application as HULApplication).appComponent.fieldAuditorDashboardComponent()
                .create()
        dashboardComponent.inject(this)
        binding.viewModel = schoolActivityViewModel

        binding.llMain.setOnClickListener { hideKeyboard() }

        binding.pictureOfSchoolNameCapture.setOnClickListener {
            schoolActivityViewModel.visitData.value
                ?.visit_1?.picture_of_school_with_name_visible
                ?.let { it1 -> prefs.setString("previewImage", it1) }
            redirectToCamera(
                0,
                schoolActivityViewModel.imageType1.value!!,
                resources.getString(R.string.picture_school_name)
            )
        }

        binding.selfieWithSchoolName.setOnClickListener {
            prefs.setString("previewImage", "")
            redirectToCamera(
                1,
                schoolActivityViewModel.imageType2.value!!,
                resources.getString(R.string.selfie_with_school_name)
            )
        }

        binding.curriculumCapture.setOnClickListener {
            prefs.setString("previewImage", "")
            redirectToCamera(
                2,
                schoolActivityViewModel.imageType3.value!!,
                resources.getString(R.string.ack_letter_picture)
            )
        }

        binding.stats.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.btnSubmit.setOnClickListener {
            if (validateFields()) {
                if (imageIndex == 0) {
                    setProgressDialog(requireContext(), "Uploading")
                    uploadImage(schoolActivityViewModel.imageUrl1.value?.toUri()!!)
                }
            }
        }

        if (allPermissionsGranted()) {
            checkLocationSettings()
        } else {
            requestPermission()
        }

        schoolActivityViewModel.projectInfo.value =
            Gson().fromJson(requireArguments().getString("projectInfo"), ProjectInfo::class.java)

        schoolActivityViewModel.booksHandedOver.observe(viewLifecycleOwner) { index ->
            // Update UI or perform any necessary actions
            val indexSelected =
                binding.radioGroup.indexOfChild(binding.radioGroup.findViewById(index))
            if (indexSelected == 0) {
//                binding.tickSuccess.visibility = View.VISIBLE
//                binding.tickFailure.visibility = View.GONE
                schoolActivityViewModel.isBookDistributionApproved.value = 1;
                binding.txtNoOfBooksGiven.isEnabled = false
                binding.txtNoOfBooksGiven.setText(schoolActivityViewModel.projectInfo.value?.number_of_books_distributed)
            } else if (indexSelected == 1) {
//                binding.tickSuccess.visibility = View.GONE
//                binding.tickFailure.visibility = View.VISIBLE
                schoolActivityViewModel.isBookDistributionApproved.value = 0;
                binding.txtNoOfBooksGiven.isEnabled = true
                binding.txtNoOfBooksGiven.setText(schoolActivityViewModel.projectInfo.value?.number_of_books_distributed)
            }
        }

        schoolActivityViewModel.booksDistributed.observe(viewLifecycleOwner) { index ->
            // Update UI or perform any necessary actions
            val indexSelected =
                binding.radioGroup2.indexOfChild(binding.radioGroup2.findViewById(index))
            schoolActivityViewModel.booksDistributedFlag.value =
                if (indexSelected == 0) true else false
        }

        schoolActivityViewModel.videoShown.observe(viewLifecycleOwner) { index ->
            // Update UI or perform any necessary actions
            val indexSelected =
                binding.radioGroup2.indexOfChild(binding.radioGroup3.findViewById(index))
            schoolActivityViewModel.videoShownFlag.value = if (indexSelected == 0) true else false
        }


        binding.view1.setOnClickListener {
            schoolActivityViewModel.imageUrl1.value?.let { it1 ->
                showImagePreview(
                    it1
                )
            }
        }
        binding.view2.setOnClickListener {
            schoolActivityViewModel.imageUrl2.value?.let { it1 ->
                showImagePreview(
                    it1
                )
            }
        }
        binding.view3.setOnClickListener {
            schoolActivityViewModel.imageUrl3.value?.let { it1 ->
                showImagePreview(
                    it1
                )
            }
        }

        getVisitData();

        binding.radioButton1.setOnCheckedChangeListener { group, checkedId ->

            // on below line we are displaying a toast message.
            if (checkedId) {
                schoolActivityViewModel.isBookDistributionApproved.value = 1;
                binding.edtNoOfBooksGiven.isEnabled = false
                binding.txtNoOfBooksGiven.setText(schoolActivityViewModel.projectInfo.value?.number_of_books_distributed)
            }
        }

        binding.radioButton2.setOnCheckedChangeListener { group, checkedId ->

            // on below line we are displaying a toast message.
            if (checkedId) {
                schoolActivityViewModel.isBookDistributionApproved.value = 0;
                binding.edtNoOfBooksGiven.isEnabled = true
                binding.txtNoOfBooksGiven.setText(schoolActivityViewModel.projectInfo.value?.number_of_books_distributed)
            }
        }

        binding.radioButton12.setOnCheckedChangeListener { group, checkedId ->

            // on below line we are displaying a toast message.
            if (checkedId) {
                schoolActivityViewModel.booksDistributedFlag.value = true
            }
        }

        binding.radioButton22.setOnCheckedChangeListener { group, checkedId ->

            // on below line we are displaying a toast message.
            if (checkedId) {
                schoolActivityViewModel.booksDistributedFlag.value = false
            }
        }

        binding.radioButton13.setOnCheckedChangeListener { group, checkedId ->

            // on below line we are displaying a toast message.
            if (checkedId) {
                schoolActivityViewModel.videoShownFlag.value = true
            }
        }

        binding.radioButton23.setOnCheckedChangeListener { group, checkedId ->

            // on below line we are displaying a toast message.
            if (checkedId) {
                schoolActivityViewModel.videoShownFlag.value = false
            }
        }

        return root
    }

    private fun validateFields(): Boolean {
        return if (schoolActivityViewModel.imageUrl1.value?.isEmpty() == true
            || schoolActivityViewModel.imageUrl2.value?.isEmpty() == true
            || schoolActivityViewModel.imageUrl3.value?.isEmpty() == true
            || schoolActivityViewModel.isBookDistributionApproved.value?.toString()!!
                .isEmpty() || schoolActivityViewModel.booksDistributedFlag.value == null || schoolActivityViewModel.videoShownFlag.value == null
        ) {
            Toast.makeText(requireContext(), "Please add all images", Toast.LENGTH_LONG).show()
            false
        }/* else if (schoolActivityViewModel.noOfBooksGivenToSchool.value?.isEmpty() == true) {
            Toast.makeText(requireContext(), "Please add No of books given", Toast.LENGTH_LONG)
                .show()
            false
        }*/ else {
            true
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun showImagePreview(imagePath: String) {
        val imageUri = Uri.parse(imagePath)
        val newFragment = ImagePreviewDialogFragment.newInstance(imageUri)
        newFragment.show(childFragmentManager, "image_preview")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
    }

    private fun getVisitData() {
        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            setProgressDialog(requireContext(), "Loading Visit data")
            apiController.getApiResponse(
                this,
                visitsDataModel(),
                ApiExtentions.ApiDef.GET_VISIT_DATA.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.GET_VISIT_DATA.ordinal, this)
        }
    }

    private fun saveSchoolActivityData() {
        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            apiController.getApiResponse(
                this,
                getSaveSchoolDataModel(),
                ApiExtentions.ApiDef.SAVE_SCHOOL_ACTIVITY_DATA.ordinal
            )
        } else {
            noInternetDialogue(
                requireContext(),
                ApiExtentions.ApiDef.SAVE_SCHOOL_ACTIVITY_DATA.ordinal,
                this
            )
        }
    }

    private fun visitsDataModel(): RequestModel {
        return schoolActivityViewModel.projectInfo.value?.visit_id?.let {
            RequestModel(
                project = userInfo.projectName,
                visitId = it,
                loadImages = true
            )
        }!!
    }

    private fun getSaveSchoolDataModel(): RequestModel {
        return RequestModel(
            visit_id = schoolActivityViewModel.projectInfo.value?.visit_id.toString() ?: "",
            collected_by = userInfo.userType,
            visitData = VisitData(
                no_of_teachers_trained = VisitDetails(
                    value = schoolActivityViewModel.visitData.value?.visit_1?.no_of_teachers_trained?.value,
                    is_approved = schoolActivityViewModel.visitData.value?.visit_1?.no_of_teachers_trained?.is_approved,
                    rejection_reason = schoolActivityViewModel.visitData.value?.visit_1?.no_of_teachers_trained?.rejection_reason
                ),
                picture_of_school_name = VisitDetails(
                    value = schoolActivityViewModel.imageUrl1API.value
                ),
                selfie_with_school_name_or_u_dice_code = VisitDetails(
                    value = schoolActivityViewModel.imageUrl2API.value
                ),
                picture_of_acknowledgement_letter = VisitDetails(
                    value = schoolActivityViewModel.imageUrl3API.value
                ),
                number_of_students_as_per_record = VisitDetails(
                    value = schoolActivityViewModel.visitData.value?.visit_1?.number_of_students_as_per_record?.value
                ),
                number_of_books_distributed = VisitDetails(
                    value = schoolActivityViewModel.visitData.value?.visit_1?.number_of_books_distributed?.value,
                    is_approved = schoolActivityViewModel.isBookDistributionApproved.value
                ),
                school_closed = VisitDetails(
                    value = schoolActivityViewModel.visitData.value?.visit_1?.school_closed?.value
                ),
                school_representative_who_collected_the_books = VisitDetails(
                    value = schoolActivityViewModel.visitData.value?.visit_1?.school_representative_who_collected_the_books?.value
                ),
                principal_contact_details = VisitDetails(
                    value = schoolActivityViewModel.visitData.value?.visit_1?.principal_contact_details?.value
                ),
                principal = VisitDetails(
                    value = schoolActivityViewModel.visitData.value?.visit_1?.principal?.value
                )
            )
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
        val visitPrefix =
            "visit_id" + (schoolActivityViewModel.projectInfo.value?.visit_id.toString() ?: "");
        if (imageIndex == 0) {
            fileName = visitPrefix + "_picture_of_the_school_name.jpeg";
        } else if (imageIndex == 1) {
            fileName = visitPrefix + "_selfie_with_the_school_name.jpeg";
        } else {
            fileName = visitPrefix + "_acknowledgement_letter.jpeg";
        }
        return RequestModel(
            project = schoolActivityViewModel.projectInfo.value?.project_name ?: "",
            uploadFor = "field_audit",
            filename = fileName,
            visit_id = schoolActivityViewModel.projectInfo.value?.visit_id.toString() ?: "",
        )
    }

    override fun onApiSuccess(o: String?, objectType: Int) {
        when (ApiExtentions.ApiDef.entries[objectType]) {
            ApiExtentions.ApiDef.UPLOAD_IMAGE -> {
                val model = JSONObject(o.toString())
                val uploadImageData = Gson().fromJson(
                    model.getJSONObject("data").toString(),
                    UploadImageData::class.java
                )
                if (uploadImageData != null && imageIndex == 0) {
                    imageIndex += 1;
                    schoolActivityViewModel.imageUrl1API.value = uploadImageData.url
                    uploadImage(schoolActivityViewModel.imageUrl2.value?.toUri()!!)
                } else if (uploadImageData != null && imageIndex == 1) {
                    imageIndex += 1;
                    schoolActivityViewModel.imageUrl2API.value = uploadImageData.url
                    uploadImage(schoolActivityViewModel.imageUrl2.value?.toUri()!!)
                } else if (imageIndex == 2) {
                    schoolActivityViewModel.imageUrl3API.value = uploadImageData.url
                    // All images uploaded, Need to call Submit Data Api
                    saveSchoolActivityData()
                }
            }

            ApiExtentions.ApiDef.GET_VISIT_DATA -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                schoolActivityViewModel.visitData.value = Gson().fromJson(
                    model.getJSONObject("data").toString(),
                    GetVisitDataResponseData::class.java
                )
            }

            ApiExtentions.ApiDef.SAVE_SCHOOL_ACTIVITY_DATA -> {
                cancelProgressDialog()
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(
                        R.id.action_reset_to_dashboard,
                        true
                    ) // Adjust to your actual dashboard fragment ID
                    .build()

                findNavController().navigate(R.id.action_reset_to_dashboard, null, navOptions)
            }

            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }
    }

    override fun onApiError(message: String?) {
        redirectionAlertDialogue(requireContext(), message!!)
    }

    override fun retry(type: Int) {
        when (ApiExtentions.ApiDef.entries[type]) {
            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }
    }

    private fun redirectToCamera(position: Int, imageType: String, heading: String) {
        val intent = Intent(activity, CameraActivity::class.java)
        val bundle = Bundle()
        bundle.putInt("position", position)
        bundle.putString("imageType", imageType)
        bundle.putString("heading", heading)
        intent.putExtras(bundle)
        startImageCapture.launch(intent)
    }

    private var startImageCapture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                if (data!!.getIntExtra("position", 0) == 0) {
                    schoolActivityViewModel.imageUrl1.value =
                        result.data!!.getStringExtra("imageUrl")
                } else if (data.getIntExtra("position", 0) == 1) {
                    schoolActivityViewModel.imageUrl2.value =
                        result.data!!.getStringExtra("imageUrl")
                } else if (data.getIntExtra("position", 0) == 2) {
                    schoolActivityViewModel.imageUrl3.value =
                        result.data!!.getStringExtra("imageUrl")
                }
            }
        }

    companion object {
        private const val TAG = "CameraXGFG"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 20
        val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
        )
    }

    private fun hideKeyboard() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val view = requireActivity().currentFocus
        view?.let {
            imm.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

}