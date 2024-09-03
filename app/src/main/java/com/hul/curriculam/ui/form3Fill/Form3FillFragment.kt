package com.hul.curriculam.ui.form3Fill

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
import android.os.CountDownTimer
import android.os.Looper
import android.provider.Settings
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
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
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.hul.HULApplication
import com.hul.R
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.controller.APIController
import com.hul.api.controller.UploadFileController
import com.hul.camera.CameraActivity
import com.hul.curriculam.CurriculamComponent
import com.hul.dashboard.Dashboard
import com.hul.data.GetVisitDataResponseData
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.data.SchoolCode
import com.hul.data.UploadImageData
import com.hul.data.VisitData
import com.hul.data.VisitDetails
import com.hul.databinding.FragmentForm3FillBinding
import com.hul.screens.field_auditor_dashboard.ui.image_preview.ImagePreviewDialogFragment
import com.hul.sync.VisitDataTable
import com.hul.sync.VisitDataViewModel
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import org.json.JSONObject
import javax.inject.Inject

class Form3FillFragment : Fragment(), ApiHandler, RetryInterface {
    private var _binding: FragmentForm3FillBinding? = null
    private val binding get() = _binding!!
    private lateinit var curriculamComponent: CurriculamComponent
    @Inject
    lateinit var form3FillViewModel: Form3FillViewModel
    @Inject
    lateinit var userInfo: UserInfo
    @Inject
    lateinit var apiController: APIController
    @Inject
    lateinit var uploadFileController: UploadFileController
    @Inject
    lateinit var visitDataViewModel: VisitDataViewModel
    var imageIndex: Int = 0
    private lateinit var countDownTimer: CountDownTimer
    var isTimerStarted = false;
    private var currentLocation: Location? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentForm3FillBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        curriculamComponent =
            (activity?.application as HULApplication).appComponent.curriculamComponent()
                .create()
        curriculamComponent.inject(this)

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

        val schoolCode = Gson().fromJson(
            requireArguments().getString(ARG_CONTENT1),
            SchoolCode::class.java
        )

        form3FillViewModel.projectInfo.value = Gson().fromJson(
            requireArguments().getString(ARG_CONTENT2),
            ProjectInfo::class.java
        )

        //Client asked to remove it, so hidden in both results
        binding.llGetDirection.visibility =
            if (schoolCode.lattitude == null) View.GONE else View.GONE

        form3FillViewModel.selectedSchoolCode.value = schoolCode

        form3FillViewModel.projectInfo.value = Gson().fromJson(
            requireArguments().getString(ARG_CONTENT2),
            ProjectInfo::class.java
        )
        binding.viewModel = form3FillViewModel

        binding.capture1.setOnClickListener {
            redirectToCamera(0, "Back", requireContext().getString(R.string.school_pic1))
        }
        binding.capture2.setOnClickListener {
            redirectToCamera(
                1,
                "Image Capture Front",
                requireContext().getString(R.string.school_pic2)
            )
        }
        binding.capture3.setOnClickListener {
            redirectToCamera(2, "Back", requireContext().getString(R.string.school_pic3))
        }
        binding.capture4.setOnClickListener {
            redirectToCamera(3, "Back", requireContext().getString(R.string.school_pic4))
        }

        binding.proceed.setOnClickListener {
            if (checkValidation()) {
                if (imageIndex == 0) {
                    val visitDataTable = VisitDataTable(
                        jsonData = Gson().toJson(submitModel()),
                        visitNumber = form3FillViewModel.projectInfo.value!!.visit_number!!.toInt(),
                        locationName = form3FillViewModel.projectInfo.value!!.location_name!!,
                        uDiceCode = binding.disceCode.text.toString(),
                        locationId = form3FillViewModel.projectInfo.value!!.location_id!!
                    )
                    Log.d("form3FillViewModel", "onCreateView: ${visitDataTable}")
                    visitDataViewModel.insert(visitDataTable)

                    Toast.makeText(requireContext(), "Visit Data saved successfully", Toast.LENGTH_LONG).show()

                    val intent = Intent(activity, Dashboard::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    startActivity(intent)
                    requireActivity().finish()
                }
            }
        }
        binding.view1.setOnClickListener {
            form3FillViewModel.imageUrl1.value?.let { it1 ->
                showImagePreview(
                    it1
                )
            }
        }
        binding.view2.setOnClickListener {
            form3FillViewModel.imageUrl2.value?.let { it1 ->
                showImagePreview(
                    it1
                )
            }
        }
        binding.view3.setOnClickListener {
            form3FillViewModel.imageUrl3.value?.let { it1 ->
                showImagePreview(
                    it1
                )
            }
        }

        binding.view4.setOnClickListener {
            form3FillViewModel.imageUrl4.value?.let { it1 ->
                showImagePreview(
                    it1
                )
            }
        }

        form3FillViewModel.uDiceCode.value = requireArguments().getString(U_DICE_CODE)

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

        binding.form1.filters = arrayOf(allowOnlyLettersAndSpacesFilter)

        binding.form3.filters = arrayOf(allowOnlyLettersAndSpacesFilter)

        binding.llGetDirection.setOnClickListener {
            if (currentLocation != null) {
                form3FillViewModel.selectedSchoolCode.value?.longitude?.let { it1 ->
                    form3FillViewModel.selectedSchoolCode.value?.lattitude?.let { it2 ->
                        openGoogleMapsForDirections(
                            currentLocation!!.latitude,
                            currentLocation!!.longitude,
                            it2,
                            it1
                        )
                    }
                }
            }
        }

//        form3FillViewModel.revisitApplicable.observe(viewLifecycleOwner) { index ->
//            // Update UI or perform any necessary actions
//            val indexSelected = binding.radioGroup.indexOfChild(binding.radioGroup.findViewById(index))
//            form3FillViewModel.revisitApplicableFlag.value = if(indexSelected==0) true else false
//        }

        binding.radioButton1.setOnCheckedChangeListener { group, checkedId ->

            // on below line we are displaying a toast message.
            form3FillViewModel.revisitApplicableFlag.value = true
        }

        binding.radioButton2.setOnCheckedChangeListener { group, checkedId ->

            // on below line we are displaying a toast message.
            form3FillViewModel.revisitApplicableFlag.value = false
        }

        return root
    }

    private fun showImagePreview(imagePath: String) {
        val imageUri = Uri.parse(imagePath)
        val newFragment = ImagePreviewDialogFragment.newInstance(imageUri)
        newFragment.show(childFragmentManager, "image_preview")
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
                    0 -> form3FillViewModel.imageUrl1.value = imageUrl
                    1 -> form3FillViewModel.imageUrl2.value = imageUrl
                    2 -> form3FillViewModel.imageUrl3.value = imageUrl
                    3 -> form3FillViewModel.imageUrl4.value = imageUrl
                }
            }
        }


    companion object {
        private const val ARG_CONTENT1 = "content1"
        private const val ARG_CONTENT2 = "content2"
        private const val U_DICE_CODE = "uDiceCode"

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

        fun newInstance(content1: String, content2: String, uDiceCode: String?) =
            Form3FillFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONTENT1, content1)
                    putString(ARG_CONTENT2, content2)
                    putString(U_DICE_CODE, uDiceCode)
                }
            }
    }

    private fun visitsDataModel(): RequestModel {
        return form3FillViewModel.projectInfo.value?.visit_id?.let {
            RequestModel(
                project = userInfo.projectName,
                visitId = it,
                loadImages = false
            )
        }!!
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
        getVisitData()
    }

    // TODO: Step 1.1, Review variables (no changes).
// FusedLocationProviderClient - Main class for receiving location updates.
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // LocationRequest - Requirements for the location updates, i.e., how often you
// should receive updates, the priority, etc.
    private lateinit var locationRequest: LocationRequest

    // LocationCallback - Called when FusedLocationProviderClient has a new Location.
    private lateinit var locationCallback: LocationCallback

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
        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())


        locationRequest = LocationRequest.create().apply {
            interval = 60
            fastestInterval = 30
            maxWaitTime = 10

            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                currentLocation = locationResult.lastLocation
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)

            }
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )

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
            visit_number = "3",
            project = userInfo.projectName,
            visit_id = form3FillViewModel.projectInfo.value!!.visit_id.toString(),
            visitData = VisitData(
                u_dice_code = VisitDetails(value = binding.disceCode.text.toString()),
                school_name = VisitDetails(value = binding.schoolName.text.toString()),
                number_of_books_distributed = VisitDetails(value = binding.noOfBooksHanded.text.toString()),
                visit_image_1 = VisitDetails(value = form3FillViewModel.imageUrl1.value),
                visit_image_2 = VisitDetails(value = form3FillViewModel.imageUrl2.value),
                visit_image_3 = VisitDetails(value = form3FillViewModel.imageUrl3.value),
                visit_image_4 = VisitDetails(value = form3FillViewModel.imageUrl4.value),
                name_of_the_school_representative_who_collected_the_books = VisitDetails(value = binding.form1.text.toString()),
                mobile_number_of_the_school_representative_who_collected_the_books = VisitDetails(
                    value = binding.form2.text.toString()
                ),
                no_of_filled_trackers_collected = VisitDetails(value = binding.noOfFilledTrackersCollected.text.toString()),
                name_of_the_principal = VisitDetails(value = binding.form3.text.toString()),
                mobile_number_of_the_principal = VisitDetails(value = binding.form4.text.toString()),
                revisit_applicable = VisitDetails(value = form3FillViewModel.revisitApplicableFlag.value),
                remark = VisitDetails(value = form3FillViewModel.form5.value),
                visit_id = form3FillViewModel.projectInfo.value!!.visit_id.toString(),
                latitude = VisitDetails(value = currentLocation?.latitude.toString()),
                longitude = VisitDetails(value = currentLocation?.longitude.toString())
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

            ApiExtentions.ApiDef.UPLOAD_IMAGE -> {
                val model = JSONObject(o.toString())
                val uploadImageData = Gson().fromJson(
                    model.getJSONObject("data").toString(),
                    UploadImageData::class.java
                )
                if (uploadImageData != null && imageIndex == 0) {
                    imageIndex += 1;
                    form3FillViewModel.imageApiUrl1.value = uploadImageData.url
                    uploadImage(form3FillViewModel.imageUrl2.value?.toUri()!!)
                } else if (uploadImageData != null && imageIndex == 1) {
                    imageIndex += 1;
                    form3FillViewModel.imageApiUrl2.value = uploadImageData.url
                    uploadImage(form3FillViewModel.imageUrl3.value?.toUri()!!)
                } else if (uploadImageData != null && imageIndex == 2) {
                    imageIndex += 1;
                    form3FillViewModel.imageApiUrl3.value = uploadImageData.url
                    uploadImage(form3FillViewModel.imageUrl4.value?.toUri()!!)
                } else if (uploadImageData != null && imageIndex == 3) {
                    imageIndex += 1;
                    form3FillViewModel.imageApiUrl4.value = uploadImageData.url
                    submitForm()
                }
            }

            ApiExtentions.ApiDef.GET_VISIT_DATA -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                form3FillViewModel.visitData.value = Gson().fromJson(
                    model.getJSONObject("data").toString(),
                    GetVisitDataResponseData::class.java
                )
                fillData()
            }

            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }
    }

    private fun fillData() {
        binding.disceCode.setText(form3FillViewModel.uDiceCode.value)
        binding.schoolName.setText(form3FillViewModel.selectedSchoolCode.value?.location_name)
        binding.noOfBooksHanded.setText(form3FillViewModel.projectInfo.value?.number_of_books_distributed)

        binding.form1.setText(
            form3FillViewModel.visitData.value?.visit_3?.name_of_the_school_representative_who_collected_the_books?.value?.toString()
                ?: ""
        )
        binding.form2.setText(
            form3FillViewModel.visitData.value?.visit_3?.mobile_number_of_the_school_representative_who_collected_the_books?.value?.toString()
                ?: ""
        )
        binding.form3.setText(
            form3FillViewModel.visitData.value?.visit_3?.name_of_the_principal?.value?.toString()
                ?: ""
        )
        binding.form4.setText(
            form3FillViewModel.visitData.value?.visit_3?.mobile_number_of_the_principal?.value?.toString()
                ?: ""
        )
        binding.form5.setText(
            form3FillViewModel.visitData.value?.visit_3?.remark?.value?.toString() ?: ""
        )

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
        binding.llTimer.visibility = View.VISIBLE

        countDownTimer = object : CountDownTimer(totalTime, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimerText(millisUntilFinished)
            }

            override fun onFinish() {
                binding.llTimer.visibility = View.GONE
                form3FillViewModel.timerFinished.value = true
            }
        }

        countDownTimer.start()
    }

    private fun updateTimerText(millisUntilFinished: Long) {
        val minutesLeft = millisUntilFinished / 1000 / 60
        val secondsLeft = (millisUntilFinished / 1000) % 60
        binding.txtClock.text = String.format("Submit in %d:%02d minutes", minutesLeft, secondsLeft)
    }

    private fun checkValidation(): Boolean {
        if (binding.disceCode.text.toString().isBlank()) {
            Toast.makeText(requireContext(), "Please enter U Dise Code", Toast.LENGTH_LONG).show()
            return false;
        } else if (binding.schoolName.text.toString().isBlank()) {
            Toast.makeText(requireContext(), "Please enter school name", Toast.LENGTH_LONG).show()
            return false;
        } else if (binding.noOfBooksHanded.text.toString().isBlank()) {
            Toast.makeText(
                requireContext(),
                "Please enter no. of books handed over",
                Toast.LENGTH_LONG
            ).show()
            binding.noOfBooksHanded.isEnabled = true
            return false;
        } else if (form3FillViewModel.form1.value.toString().isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please enter name of school representative",
                Toast.LENGTH_LONG
            ).show()
            return false;
        } else if (form3FillViewModel.form2.value.toString().isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please enter mobile number of school representative",
                Toast.LENGTH_LONG
            ).show()
            return false;
        } else if (currentLocation == null) {
            Toast.makeText(requireContext(), "Location details not found", Toast.LENGTH_LONG).show()
            return false;
        } else if (form3FillViewModel.imageUrl1.value?.isEmpty() == true || form3FillViewModel.imageUrl2.value?.isEmpty() == true
            || form3FillViewModel.imageUrl3.value?.isEmpty() == true || form3FillViewModel.imageUrl4.value?.isEmpty() == true
        ) {
            Toast.makeText(requireContext(), "Please add required images", Toast.LENGTH_LONG).show()
            return false;
        } else {
            return true;
        }
    }
}