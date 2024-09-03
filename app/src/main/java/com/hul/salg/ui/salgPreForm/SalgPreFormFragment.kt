package com.hul.salg.ui.salgPreForm

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.InputFilter
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.findNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.hul.HULApplication
import com.hul.R
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.controller.APIController
import com.hul.api.controller.UploadFileController
import com.hul.camera.CameraActivity
import com.hul.dashboard.Dashboard
import com.hul.data.RequestModel
import com.hul.data.Society
import com.hul.data.VisitData
import com.hul.data.VisitDetails
import com.hul.databinding.FragmentSalgFormFillBinding
import com.hul.databinding.FragmentSalgPreFormBinding
import com.hul.salg.SalgDashboard
import com.hul.salg.SalgDashboardComponent
import com.hul.salg.ui.formFill.SalgFormFillFragment
import com.hul.salg.ui.formFill.SalgFormFillViewModel
import com.hul.screens.field_auditor_dashboard.ui.image_preview.ImagePreviewDialogFragment
import com.hul.sync.SocietyVisitDataTable
import com.hul.sync.SocietyVisitDataViewModel
import com.hul.sync.VisitDataTable
import com.hul.sync.VisitDataViewModel
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class SalgPreFormFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentSalgPreFormBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var salgDashboardComponent: SalgDashboardComponent

//    private lateinit var disceCodeEditText: String

    @Inject
    lateinit var salgPreFormViewModel: SalgPreFormViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    @Inject
    lateinit var uploadFileController: UploadFileController

    @Inject
    lateinit var visitDataViewModel: SocietyVisitDataViewModel

    var imageIndex: Int = 0

    private lateinit var countDownTimer: CountDownTimer

    var isTimerStarted = false;

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSalgPreFormBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        salgDashboardComponent =
            (activity?.application as HULApplication).appComponent.salgDashboardComponent()
                .create()
        salgDashboardComponent.inject(this)

        salgPreFormViewModel.projectInfo.value =
            Gson().fromJson(requireArguments().getString("projectInfo"), Society::class.java)
        binding.societyName.text = salgPreFormViewModel.projectInfo.value!!.location_name
        binding.visitSubTitle.text = salgPreFormViewModel.projectInfo.value!!.location_name
//        binding.llGetDirection.visibility =
//            if (schoolCode?.lattitude == null) View.GONE else View.GONE


        binding.viewModel = salgPreFormViewModel
        binding.stats.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.capture1.setOnClickListener {
            redirectToCamera(0, "Back", requireContext().getString(R.string.upload_picture_of_door))
        }

        binding.proceed.setOnClickListener {
            if(checkFlatCompleted()) {
                if (checkValidation()) {
                    when (salgPreFormViewModel.responseModel.value) {
                        "Accepted" -> {
                            val bundle = Bundle()
                            bundle.putString(
                                "projectInfo",
                                Gson().toJson(salgPreFormViewModel.projectInfo.value)
                            )
                            bundle.putString("wingNumber", salgPreFormViewModel.wingNumber.value)
                            bundle.putString("floor", salgPreFormViewModel.floor.value)
                            bundle.putString("flatNumber", salgPreFormViewModel.flatNumber.value)
                            bundle.putString("imageUrl1", salgPreFormViewModel.imageUrl1.value)
                            bundle.putString("response", salgPreFormViewModel.responseModel.value)
                            findNavController().navigate(
                                R.id.action_salgPreFormFragment_to_salgFormFragment,
                                bundle
                            )
                        }

                        "Rejected" -> {

                            val visitDataTable = SocietyVisitDataTable(
                                jsonData = Gson().toJson(submitModel()),
                                visitNumber = 1,
                                locationName = salgPreFormViewModel.projectInfo.value!!.location_name!!,
                                locationId = salgPreFormViewModel.projectInfo.value!!.id!!.toString(),
                                floor = salgPreFormViewModel.floor.value!!,
                                wingNumber = salgPreFormViewModel.wingNumber.value!!,
                                flatNumber = salgPreFormViewModel.flatNumber.value!!
                            )

                            visitDataViewModel.insert(visitDataTable)

                        }

                        "Door not opened" -> {

                            val visitDataTable = SocietyVisitDataTable(
                                jsonData = Gson().toJson(submitModel()),
                                visitNumber = 1,
                                locationName = salgPreFormViewModel.projectInfo.value!!.location_name!!,
                                locationId = salgPreFormViewModel.projectInfo.value!!.id!!.toString(),
                                floor = salgPreFormViewModel.floor.value!!,
                                wingNumber = salgPreFormViewModel.wingNumber.value!!,
                                flatNumber = salgPreFormViewModel.flatNumber.value!!
                            )

                            visitDataViewModel.insert(visitDataTable)

                        }

                        "Come back later" -> {

                            val visitDataTable = SocietyVisitDataTable(
                                jsonData = Gson().toJson(submitModelRevisit()),
                                visitNumber = 1,
                                locationName = salgPreFormViewModel.projectInfo.value!!.location_name!!,
                                locationId = salgPreFormViewModel.projectInfo.value!!.id!!.toString(),
                                floor = salgPreFormViewModel.floor.value!!,
                                wingNumber = salgPreFormViewModel.wingNumber.value!!,
                                flatNumber = salgPreFormViewModel.flatNumber.value!!
                            )

                            visitDataViewModel.insert(visitDataTable)

                        }

                        "Link to another apartment" -> {

                            val visitDataTable = SocietyVisitDataTable(
                                jsonData = Gson().toJson(submitModelLink()),
                                visitNumber = 1,
                                locationName = salgPreFormViewModel.projectInfo.value!!.location_name!!,
                                locationId = salgPreFormViewModel.projectInfo.value!!.id!!.toString(),
                                floor = salgPreFormViewModel.floor.value!!,
                                wingNumber = salgPreFormViewModel.wingNumber.value!!,
                                flatNumber = salgPreFormViewModel.flatNumber.value!!
                            )

                            visitDataViewModel.insert(visitDataTable)

                        }
                    }


                    if (salgPreFormViewModel.responseModel.value != "Accepted") {
                        Toast.makeText(
                            requireContext(),
                            "Visit Data saved successfully",
                            Toast.LENGTH_LONG
                        ).show()

                        val intent = Intent(activity, SalgDashboard::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        startActivity(intent)
                        requireActivity().finish()
                    }
                }
            }else{
                Toast.makeText(
                    requireContext(),
                    "Flat already added",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.view1.setOnClickListener {
            salgPreFormViewModel.imageUrl1.value?.let { it1 ->
                showImagePreview(
                    it1
                )
            }
        }

        val responseArray = arrayListOf(
            "Accepted",
            "Rejected",
            "Door not opened",
            "Come back later",
            "Link to another apartment"
        )
        val responseArrayAdapter =
            ArrayAdapter(requireActivity(), R.layout.list_popup_window_item, responseArray)
        binding.response.setAdapter(responseArrayAdapter)

        val floorArray = arrayListOf(
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "10",
            "11",
            "12",
            "13",
            "14",
            "15",
            "16",
            "17",
            "18",
            "19",
            "20",
            "21",
            "22",
            "23",
            "24",
            "25",
            "26",
            "27",
            "28",
            "29",
            "30",
            "31",
            "32",
            "33",
            "34",
            "35",
            "36",
            "37",
            "38",
            "39",
            "40",
            "41",
            "42",
            "43",
            "44",
            "45",
            "46",
            "47",
            "48",
            "49",
            "50"
        )
        val floorArrayAdapter =
            ArrayAdapter(requireActivity(), R.layout.list_popup_window_item, floorArray)
        binding.floor.setAdapter(floorArrayAdapter)

        val calendar = Calendar.getInstance()

        // Define the date format you want to display
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        binding.date.setOnClickListener {
            var datePickerDialog = DatePickerDialog(
                requireContext(),
                { _: DatePicker, year: Int, month: Int, day: Int ->
                    // Set the selected date in the TextInputEditText
                    calendar.set(year, month, day)
                    binding.date.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.datePicker.minDate = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            datePickerDialog.datePicker.maxDate = calendar.timeInMillis
            datePickerDialog.show()
        }

        val calendarTime = Calendar.getInstance()
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)

        binding.clock.setOnClickListener {
            TimePickerDialog(
                requireContext(),
                { _: TimePicker, hour: Int, minute: Int ->
                    calendarTime.set(Calendar.HOUR_OF_DAY, hour)
                    calendarTime.set(Calendar.MINUTE, minute)
                    binding.clock.setText(timeFormat.format(calendarTime.time))
                },
                calendarTime.get(Calendar.HOUR_OF_DAY),
                calendarTime.get(Calendar.MINUTE),
                false
            ).show()

        }

        if (allPermissionsGranted()) {
            checkLocationSettings()
        } else {
            requestPermission()
        }


        binding.nestedScrollView.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                    Log.d("TouchListener", "User scrolled manually")
                    val currentFocus = activity?.currentFocus
                    if (currentFocus is TextInputEditText) {
                        currentFocus.clearFocus()
                    }
                }
            }
            false
        }

//        form1FillViewModel.revisitApplicable.observe(viewLifecycleOwner) { index ->
//            // Update UI or perform any necessary actions
//            val indexSelected = binding.radioGroup.indexOfChild(binding.radioGroup.findViewById(index))
//            form1FillViewModel.revisitApplicableFlag.value = if(indexSelected==0) true else false
//        }
        return root
    }

    private fun checkFlatCompleted() : Boolean
    {
        for(flat in salgPreFormViewModel.projectInfo.value!!.flats_completed!!)
        {
            if (salgPreFormViewModel.wingNumber.value.equals(flat.wing) && salgPreFormViewModel.flatNumber.value.equals(flat.flat))
            {
                return false
            }

        }

        return true
    }

    private fun submitModelRevisit(): RequestModel {

        return RequestModel(
            visit_number = "1",
            project = userInfo.projectName,
            //visit_id = formFillViewModel.projectInfo.value!!.visit_id.toString(),

            visitData = VisitData(
                visit_image_1 = VisitDetails(value = salgPreFormViewModel.imageUrl1.value),
                latitude = VisitDetails(value = currentLocation?.latitude.toString()),
                longitude = VisitDetails(value = currentLocation?.longitude.toString()),
                response = VisitDetails(value = salgPreFormViewModel.responseModel.toString()),
                select_date_for_visit = VisitDetails(value = salgPreFormViewModel.date.toString()),
                select_time_for_visit = VisitDetails(value = salgPreFormViewModel.time.toString()),
                wing_number = VisitDetails(value = salgPreFormViewModel.wingNumber.value),
                floor = VisitDetails(value = salgPreFormViewModel.floor.value),
                flatNumber = VisitDetails(value = salgPreFormViewModel.flatNumber.value),
            )
        )
    }

    private fun submitModelLink(): RequestModel {

        return RequestModel(
            visit_number = "1",
            project = userInfo.projectName,
            //visit_id = formFillViewModel.projectInfo.value!!.visit_id.toString(),

            visitData = VisitData(
                visit_image_1 = VisitDetails(value = salgPreFormViewModel.imageUrl1.value),
                latitude = VisitDetails(value = currentLocation?.latitude.toString()),
                longitude = VisitDetails(value = currentLocation?.longitude.toString()),
                response = VisitDetails(value = salgPreFormViewModel.responseModel.toString()),
                enter_another_apartment = VisitDetails(value = salgPreFormViewModel.anotherFlatNumber.toString()),
                wing_number = VisitDetails(value = salgPreFormViewModel.wingNumber.value),
                floor = VisitDetails(value = salgPreFormViewModel.floor.value),
                flatNumber = VisitDetails(value = salgPreFormViewModel.flatNumber.value),
            )
        )
    }

    private fun checkValidation(): Boolean {
        var validationSuccess = true
        if (salgPreFormViewModel.wingNumber.value.isNullOrEmpty()) {
            validationSuccess = false
            salgPreFormViewModel.wingNumberError.value = "Enter wing name/number"
        }

        if (salgPreFormViewModel.floor.value.isNullOrEmpty()) {
            validationSuccess = false
            salgPreFormViewModel.floorError.value = "Select floor"
        }

        if (salgPreFormViewModel.flatNumber.value.isNullOrEmpty()) {
            validationSuccess = false
            salgPreFormViewModel.flatNumberError.value = "Enter Flat number"
        }

        if (salgPreFormViewModel.imageUrl1.value.isNullOrEmpty()) {
            validationSuccess = false
            Toast.makeText(
                requireContext(),
                "Please upload picture of door",
                Toast.LENGTH_LONG
            ).show()
        }

        when (salgPreFormViewModel.responseModel.value) {
            "Accepted" -> {

            }

            "Rejected" -> {

            }

            "Door not opened" -> {

            }

            "Come back later" -> {
                if (salgPreFormViewModel.date.value.isNullOrEmpty()) {
                    validationSuccess = false
                    salgPreFormViewModel.dateError.value = "Select date"
                }

                if (salgPreFormViewModel.time.value.isNullOrEmpty()) {
                    validationSuccess = false
                    salgPreFormViewModel.timeError.value = "Select time"
                }
            }

            "Link to another apartment" -> {
                if (salgPreFormViewModel.anotherFlatNumber.value.isNullOrEmpty()) {
                    validationSuccess = false
                    salgPreFormViewModel.anotherFlatNumberError.value = "Select date"
                }
            }

            "" -> {
                validationSuccess = false
                salgPreFormViewModel.responseModelError.value = "Select Value"
            }

        }



        return validationSuccess;
    }

    private fun openGoogleMapsForDirections(
        lat: Double,
        lng: Double,
        destinationLat: String,
        destinationLng: String
    ) {

        val destLat = destinationLat
        val destLng = destinationLng

        // Build the URI for the directions request
        val uri =
            Uri.parse("http://maps.google.com/maps?saddr=$lat,$lng&daddr=$destLat,$destLng")

        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    private fun showImagePreview(imagePath: String) {
        val imageUri = Uri.parse(imagePath)
        val newFragment = ImagePreviewDialogFragment.newInstance(imageUri)
        newFragment.show(childFragmentManager, "image_preview")
    }


    private fun redirectToCamera(position: Int, imageType: String, heading: String) {
        val intent = Intent(activity, CameraActivity::class.java)
        intent.putExtra("position", position)
        intent.putExtra("imageType", imageType)
        intent.putExtra("heading", heading)
        startImageCapture.launch(intent)
    }

    private fun uploadImageModel(): RequestModel {
        var fileName: String = ""
        val visitPrefix = "project_" + userInfo.projectName;
        when (imageIndex) {
            0 -> {
                fileName = visitPrefix + "_picture_of_school_name.jpeg";
            }

            1 -> {
                fileName = visitPrefix + "_selfie_with_school_name.jpeg";
            }

            2 -> {
                fileName = visitPrefix + "_students_showing_filled_tracker.jpeg";
            }

            3 -> {
                fileName = visitPrefix + "_teacher_handling_trackers.jpeg";
            }

            4 -> {
                fileName = visitPrefix + "_acknowledgement_letter.jpeg";
            }
        }

        return RequestModel(
            project = userInfo.projectName,
            uploadFor = "field_audit",
            filename = fileName
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

    val startImageCapture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val position = data!!.getIntExtra("position", 0)
                val imageUrl = result.data!!.getStringExtra("imageUrl")

                startTimer()

                // Update the view model's imageUrl at the corresponding position
                when (position) {
                    0 -> salgPreFormViewModel.imageUrl1.value = imageUrl
                }
            }
        }


    companion object {
        private const val ARG_CONTENT1 = "content1"
        private const val ARG_CONTENT2 = "content2"
        private const val U_DICE_CODE = "uDiceCode"

        private val REQUIRED_PERMISSIONS =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                )
            } else {
                arrayOf(
                    Manifest.permission.CAMERA,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                )

            }

        fun newInstance(content1: String, content2: String, uDiceCode: String?) =
            SalgFormFillFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONTENT1, content1)
                    putString(ARG_CONTENT2, content2)
                    putString(U_DICE_CODE, uDiceCode)
                }
            }
    }

    private fun visitsDataModel(): RequestModel {
        return RequestModel(
            project = userInfo.projectName,
            loadImages = false
        )
    }

    private fun getVisitData() {
        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            //setProgressDialog(requireContext(), "Loading Visit data")
            apiController.getApiResponse(
                this,
                visitsDataModel(),
                ApiExtentions.ApiDef.GET_VISIT_DATA.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.GET_VISIT_DATA.ordinal, this)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

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
            if (!allPermissionsGranted()) {
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

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    fun submitForm() {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            //setProgressDialog(requireContext(), "Loading Leads")
            apiController.getApiResponse(
                this,
                submitModel(),
                ApiExtentions.ApiDef.VISIT_DATA.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.VISIT_DATA.ordinal, this)
        }

    }

    private fun submitModel(): RequestModel {

        return RequestModel(
            visit_number = "1",
            project = userInfo.projectName,
            //visit_id = formFillViewModel.projectInfo.value!!.visit_id.toString(),
            visitData = VisitData(
                visit_image_1 = VisitDetails(value = salgPreFormViewModel.imageUrl1.value),
                latitude = VisitDetails(value = currentLocation?.latitude.toString()),
                longitude = VisitDetails(value = currentLocation?.longitude.toString()),
                response = VisitDetails(value = salgPreFormViewModel.responseModel.toString()),
                wing_number = VisitDetails(value = salgPreFormViewModel.wingNumber.value),
                floor = VisitDetails(value = salgPreFormViewModel.floor.value),
                flatNumber = VisitDetails(value = salgPreFormViewModel.flatNumber.value),
            )
        )
    }

    override fun onApiSuccess(o: String?, objectType: Int) {
        when (ApiExtentions.ApiDef.entries[objectType]) {

            ApiExtentions.ApiDef.SUBMIT_SCHOOL_FORM -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    // Set the adapter to the AutoCompleteTextView
                    requireActivity().onBackPressed()
                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }
            }

            ApiExtentions.ApiDef.VISIT_DATA -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    userInfo.didUserSubmitNewVisit = true
                    val intent = Intent(activity, Dashboard::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    requireActivity().finish()
                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }
            }

            ApiExtentions.ApiDef.GET_VISIT_DATA -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
//                formFillViewModel.visitData.value = Gson().fromJson(
//                    model.getJSONObject("data").toString(),
//                    GetVisitDataResponseData::class.java
//                )

                // For render purpose only
                /*if (form1FillViewModel.visitData.value?.visit_1 != null) {
                    form1FillViewModel.visitDataToView.value =
                        form1FillViewModel.visitData.value?.visit_1
                } else if (form1FillViewModel.visitData.value?.visit_2 != null) {
                    form1FillViewModel.visitDataToView.value =
                        form1FillViewModel.visitData.value?.visit_2
                } else if (form1FillViewModel.visitData.value?.visit_3 != null) {
                    form1FillViewModel.visitDataToView.value =
                        form1FillViewModel.visitData.value?.visit_3
                }*/
            }

            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }
    }

    override fun onApiError(message: String?) {
        cancelProgressDialog()
        println(message)
        redirectionAlertDialogue(requireContext(), message!!)
    }

    override fun retry(type: Int) {

        when (ApiExtentions.ApiDef.entries[type]) {
            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }

    }

    private fun startTimer() {
        if (isTimerStarted) {
            return
        }

        isTimerStarted = true
        //binding.proceed.isEnabled = false

        val totalTime = 20 * 60 * 1000L

        // Set initial time before starting the timer
        updateTimerText(totalTime)
        //binding.llTimer.visibility = View.VISIBLE

        countDownTimer = object : CountDownTimer(totalTime, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimerText(millisUntilFinished)
            }

            override fun onFinish() {
                //binding.llTimer.visibility = View.GONE
                //form1FillViewModel.timerFinished.value = true
            }
        }

        countDownTimer.start()
    }

    private fun updateTimerText(millisUntilFinished: Long) {
        val minutesLeft = millisUntilFinished / 1000 / 60
        val secondsLeft = (millisUntilFinished / 1000) % 60
        //binding.txtClock.text = String.format("Submit in %d:%02d minutes", minutesLeft, secondsLeft)
    }

    fun showViewTemporarily(view: View, duration: Long) {
        view.visibility = View.VISIBLE
        handler.postDelayed({
            view.visibility = View.GONE
        }, duration)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }

}