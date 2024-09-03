package com.hul.web_form.dynamicFormFill

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Looper
import android.provider.Settings
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
import com.hul.data.FormElement
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.data.UploadImageData
import com.hul.databinding.FragmentDynamicForm1FillBinding
import com.hul.screens.field_auditor_dashboard.ui.image_preview.ImagePreviewDialogFragment
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import com.hul.utils.setProgressDialog
import com.hul.web_form.WebFormComponent
import org.json.JSONObject
import java.io.File
import java.lang.reflect.Type
import javax.inject.Inject


class DynamicFormFillFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentDynamicForm1FillBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var webFormComponent: WebFormComponent

    private var imageCount = 0

    private var recordAudioFlag = false

//    private lateinit var disceCodeEditText: String

    private var imageUploadPosition = 1

    @Inject
    lateinit var dynamicFormFillViewModel: DynamicFormFillViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    @Inject
    lateinit var uploadFileController: UploadFileController

    var imageList = ArrayList<ImageLinks>()

    var requestObject = JSONObject()

    var visitInfo = ProjectInfo()

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

    var isTimerStarted = false;

    private lateinit var countDownTimer: CountDownTimer

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

    private lateinit var formElementList: ArrayList<FormElement>

    companion object {
        private const val ARG_CONTENT1 = "content1"
        private const val ARG_CONTENT2 = "content2"
        private const val U_DICE_CODE = "uDiceCode"
        fun newInstance(content1: String) =
            DynamicFormFillFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONTENT1, content1)
                }
            }
    }

    private val viewModel: DynamicFormFillViewModel by viewModels()

    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String = ""

    private fun startRecording() {
        setupMediaRecorder()
        mediaRecorder?.start()
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
        } catch (e: RuntimeException) {
            e.printStackTrace()
        }
//        mediaRecorder?.apply {
//            stop()
//            release()
//        }
//        mediaRecorder = null
    }

    private fun getAudioFileUri(): Uri {
        return Uri.fromFile(File(audioFilePath))
    }

    private fun storeAudioUri() {
        val audioUri = getAudioFileUri()
        //uploadAudio(audioUri)
    }

    private fun setupMediaRecorder() {
        mediaRecorder = MediaRecorder()
        mediaRecorder?.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            audioFilePath = "${requireContext().externalCacheDir?.absolutePath}/audiorecordtest.3gp"
            setOutputFile(audioFilePath)
            prepare()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDynamicForm1FillBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        webFormComponent =
            (activity?.application as HULApplication).appComponent.webFormComponent()
                .create()
        webFormComponent.inject(this)
        visitInfo = Gson().fromJson(requireArguments().getString(ARG_CONTENT1), ProjectInfo::class.java)
        if (allPermissionsGranted()) {
            checkLocationSettings()
        } else {
            requestPermission()
        }
        binding.proceed.setOnClickListener { validateFields() }
        getVisitData()
        return root
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity(),
            it
        ) == PackageManager.PERMISSION_GRANTED
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


    fun requestPermission() {
        requestPermission.launch(REQUIRED_PERMISSIONS)
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
    }

    private fun createForm(formElements: List<FormElement>) {
        for (element in formElements) {
            when (element.input_type) {
                "Textbox" -> inflateEditTextLayout(element)
                "Image Capture" -> inflateCaptureLayout(element)
                "Dropdown" -> inflateSpinnerLayout(element)
            }
        }

//        val view = layoutInflater.inflate(R.layout.layout_button, binding.formContainer, false)
//        binding.formContainer.addView(view)
//        view.findViewById<Button>(R.id.proceed).setOnClickListener {
//            validateFields()
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder?.release()
        mediaRecorder = null
    }

    private fun validateFields() {

        if(imageCount == imageList.size) {
            requestObject = JSONObject()
            imageUploadPosition = 0
            try {
                binding.formContainer.children.forEach { formLayout ->
                    if (formLayout is LinearLayout) {
                        formLayout.children.forEach { view ->
                            when (view) {
                                is TextInputLayout -> {
                                    val editText = view.editText
                                    if (editText is TextInputEditText) {
                                        if (editText.text.toString().isEmpty()) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Fill all data",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            throw BreakException()
                                        } else {
                                            val element = getObjectUsingTag(editText.getTag().toString())
                                            if(element.input_regex != null) {
                                                if (editText.text.toString()
                                                        .matches(element.input_regex.toRegex())
                                                )
                                                {
                                                    var fieldobject = JSONObject()
                                                    fieldobject.put("is_approved", 0)
                                                    fieldobject.put("rejection_reason", "")
                                                    fieldobject.put("value", editText.text.toString())
                                                    fieldobject.put("type", "text")
                                                    requestObject.put(
                                                        editText.getTag().toString(),
                                                        fieldobject
                                                    )
                                                }
                                                else{
                                                    editText.setError("Enter correct "+element.form_field_title)
                                                    throw BreakException()
                                                }
                                            }
                                            else{
                                                var fieldobject = JSONObject()
                                                fieldobject.put("is_approved", 0)
                                                fieldobject.put("rejection_reason", "")
                                                fieldobject.put("value", editText.text.toString())
                                                fieldobject.put("type", "text")
                                                requestObject.put(
                                                    editText.getTag().toString(),
                                                    fieldobject
                                                )
                                            }
                                        }

                                    } else if (editText is AutoCompleteTextView) {
                                        if (editText.text.toString().isEmpty()) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Fill all data",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            throw BreakException()
                                        } else {
                                            var fieldobject = JSONObject()
                                            fieldobject.put("is_approved", 0)
                                            fieldobject.put("rejection_reason", "")
                                            fieldobject.put("value", editText.text.toString())
                                            fieldobject.put("type", "text")
                                            requestObject.put(
                                                editText.getTag().toString(),
                                                fieldobject
                                            )

                                        }
                                    }
                                }

                                is CheckBox -> {

                                }
                            }
                        }
                    }
                }
                //uploadImage(imageList.get(imageUploadPosition++))
                if(recordAudioFlag) {
                    stopRecording()
                    storeAudioUri()
                    uploadImage(imageList.get(imageUploadPosition++))
                }
                else{
                    uploadImage(imageList.get(imageUploadPosition++))
                }
            } catch (e: BreakException) {
                // Handle the break
                println("Caught BreakException, exiting loops")
            }
        }else{
            Toast.makeText(
                requireContext(),
                "Upload all images",
                Toast.LENGTH_LONG
            ).show()
        }

    }

    private fun getObjectUsingTag(string : String) : FormElement
    {
        for (element in formElementList)
        {
            val tag =  element.form_field_title.replace(" ", "_").lowercase()
            if(tag.equals(string))
            {
                return element
            }
        }
        return TODO("Provide the return value")
    }

    private fun inflateEditTextLayout(element: FormElement) {
        val view =
            layoutInflater.inflate(R.layout.layout_edittext, binding.formContainer, false)
        view.findViewById<TextView>(R.id.label).text = element.form_field_title
        if (element.form_field_conversion_type == "str") {
//            val allowOnlyLettersAndSpacesFilter =
//                InputFilter { source, start, end, dest, dstart, dend ->
//                    for (i in start until end) {
//                        if (!source[i].isLetter() && !source[i].isWhitespace()) {
//                            return@InputFilter ""
//                        }
//                    }
//                    null
//                }
//            view.findViewById<TextInputEditText>(R.id.editText).filters =
//                arrayOf(allowOnlyLettersAndSpacesFilter)
        } else {
            view.findViewById<TextInputEditText>(R.id.editText).inputType =
                InputType.TYPE_CLASS_NUMBER
        }
        view.findViewById<TextInputEditText>(R.id.editText).isEnabled =
            if (element.is_editable == 1) true else false
        view.findViewById<TextInputEditText>(R.id.editText).tag = getTag(element.form_field_title)
        //view.findViewById<EditText>(R.id.editText).hint = element.placeholder
        binding.formContainer.addView(view)
    }

    private fun inflateRadioButtonLayout(element: FormElement) {
        val view = layoutInflater.inflate(
            R.layout.layout_radiobutton,
            binding.formContainer,
            false
        )
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
        imageCount++
        val view = layoutInflater.inflate(
            R.layout.layout_file_upload,
            binding.formContainer,
            false
        )
        view.tag = getTag(element.form_field_title)
        view.findViewById<TextView>(R.id.label).text = element.form_field_title
        view.findViewById<AppCompatButton>(R.id.capture).setOnClickListener {
            redirectToCamera(0, element.input_allowed_values,element.form_field_title,view.tag.toString())
        }
        view.findViewById<AppCompatButton>(R.id.capture).isEnabled =
            if (element.is_editable == 1) true else false
        view.findViewById<ImageView>(R.id.view1).setOnClickListener {
            showImagePreview(view.findViewById<TextView>(R.id.url).text as String)
        }
        binding.formContainer.addView(view)
        if(imageCount==1 && recordAudioFlag)
        {
            val view1 = layoutInflater.inflate(
                R.layout.audio_input_layout,
                binding.formContainer,
                false
            )
            view1.tag = getTag("audio_recorder")
            view1.visibility = View.GONE
            binding.formContainer.addView(view1)
        }
    }

    private fun inflateSpinnerLayout(element: FormElement) {
        val view =
            layoutInflater.inflate(R.layout.layout_dropdown, binding.formContainer, false)
        view.findViewById<TextView>(R.id.label).text = element.form_field_title
        val stringArray: Array<String> =
            element.input_allowed_values.replace("'", "").split(",").toTypedArray()
        val adapter =
            ArrayAdapter(requireActivity(), R.layout.list_popup_window_item, stringArray)
        view.findViewById<AutoCompleteTextView>(R.id.dropdown).setAdapter(adapter)
        view.findViewById<AutoCompleteTextView>(R.id.dropdown).isEnabled =
            if (element.is_editable == 1) true else false
        view.findViewById<AutoCompleteTextView>(R.id.dropdown).tag = getTag(element.form_field_title)
        binding.formContainer.addView(view)
    }


    private fun getTag(string : String) : String
    {
        return string.replace(" ", "_").lowercase()
    }

    private fun redirectToCamera(position: Int, imageType: String, heading: String,tag: String) {
        val intent = Intent(requireActivity(), CameraActivity::class.java)
        intent.putExtra("position", position)
        intent.putExtra("imageType", imageType)
        intent.putExtra("heading", heading)
        intent.putExtra("tag", tag)
        startImageCapture.launch(intent)
    }

    private fun showImagePreview(imagePath: String) {
        val imageUri = Uri.parse(imagePath)
        val newFragment = ImagePreviewDialogFragment.newInstance(imageUri)
        newFragment.show(childFragmentManager, "image_preview")
    }

    val startImageCapture =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data
                val position = data!!.getIntExtra("position", 0)
                val imageUrl = result.data!!.getStringExtra("imageUrl")

                startTimer()
                if(recordAudioFlag) {
                    startRecording()
                    val audioLayout =
                        binding.formContainer.findViewWithTag<LinearLayout>("audio_recorder")
                    audioLayout.visibility = View.VISIBLE
                }
                val llLayout = binding.formContainer.findViewWithTag<LinearLayout>(result.data!!.getStringExtra("tag"))
                llLayout.findViewById<LinearLayout>(R.id.actions).visibility = View.VISIBLE
                llLayout.findViewById<AppCompatButton>(R.id.capture).visibility = View.GONE
                llLayout.findViewById<TextView>(R.id.url).text = imageUrl
                // Update the view model's imageUrl at the corresponding position
                imageList.add(ImageLinks(imageUrl!!,llLayout.findViewById<TextView>(R.id.label).text.toString()))
            }
        }

    private fun visitsDataModel(): RequestModel {
        return RequestModel(
            projectId = userInfo.projectId,
            visit_number = visitInfo.visit_number
        )
    }

    private fun startTimer() {
        if (isTimerStarted) {
            return
        }

        isTimerStarted = true
        //binding.proceed.isEnabled = false

        val totalTime = 1 * 60 * 1000L

        // Set initial time before starting the timer
        updateTimerText(totalTime)
        binding.llTimer.visibility = View.VISIBLE

        countDownTimer = object : CountDownTimer(totalTime, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimerText(millisUntilFinished)
            }

            override fun onFinish() {
                binding.llTimer.visibility = View.GONE
                binding.proceed.isEnabled = true
            }
        }

        countDownTimer.start()
    }

    private fun updateTimerText(millisUntilFinished: Long) {
        val minutesLeft = millisUntilFinished / 1000 / 60
        val secondsLeft = (millisUntilFinished / 1000) % 60
        binding.txtClock.text = String.format("Submit in %d:%02d minutes", minutesLeft, secondsLeft)
    }

    private fun getVisitData() {
        if (ConnectionDetector(requireActivity()).isConnectingToInternet()) {
            setProgressDialog(requireContext(), "Loading Visit data")
            apiController.getApiResponse(
                this,
                visitsDataModel(),
                ApiExtentions.ApiDef.GET_VISIT_FORM.ordinal
            )
        } else {
            noInternetDialogue(
                requireActivity(),
                ApiExtentions.ApiDef.GET_VISIT_FORM.ordinal,
                this
            )
        }
    }

    private fun uploadImage(image : ImageLinks) {
        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            setProgressDialog(requireContext(), "Uploading")
            uploadFileController.getApiResponse(
                this,
                image.url!!.toUri(),
                uploadImageModel(image.fileName!!),
                ApiExtentions.ApiDef.UPLOAD_IMAGE.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.UPLOAD_IMAGE.ordinal, this)
        }
    }

    private fun uploadImageModel(fileName : String): RequestModel {

        return RequestModel(
            project = userInfo.projectName,
            uploadFor = "field_audit",
            filename = fileName
        )
    }

    private fun uploadAudio(file : Uri) {
        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            setProgressDialog(requireContext(), "Uploading")
            uploadFileController.getApiResponse(
                this,
                file,
                uploadAudioModel("Audio_file"),
                ApiExtentions.ApiDef.UPLOAD_AUDIO.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.UPLOAD_AUDIO.ordinal, this)
        }
    }

    private fun uploadAudioModel(fileName : String): RequestModel {

        return RequestModel(
            project = userInfo.projectName,
            uploadFor = "field_audit",
            filename = fileName
        )
    }

    fun submitForm() {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            setProgressDialog(requireContext(), "Loading Leads")
            apiController.getApiResponse(
                this,
                submitModel(),
                ApiExtentions.ApiDef.SUBMIT_SCHOOL_FORM.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.SUBMIT_SCHOOL_FORM.ordinal, this)
        }

    }

    private fun submitModel(): RequestModel {
        var jsonObject = JSONObject()
        jsonObject.put("project", userInfo.projectName)
        jsonObject.put("visit_id", visitInfo.visit_id)
        jsonObject.put("visitData", requestObject)
        return RequestModel(form2Data = jsonObject)
        
    }

    override fun onApiSuccess(o: String?, objectType: Int) {
        when (ApiExtentions.ApiDef.entries[objectType]) {

            ApiExtentions.ApiDef.GET_VISIT_FORM -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<FormElement?>?>() {}.type
                    formElementList =
                        Gson().fromJson(
                            model.getJSONObject("data").getJSONArray("visit_tasks")
                                .getJSONObject(0).getJSONArray("form_fields").toString(),
                            listType
                        );
                    val recordAudio = model.getJSONObject("data").getJSONArray("visit_tasks").getJSONObject(0).getString("task_trigger")
                    if(recordAudio!=null && recordAudio.equals("AUDIO"))
                    {
                        recordAudioFlag = true
                    }
                    else{
                        recordAudioFlag = false
                    }
                    createForm(formElementList)
                } else {
                    redirectionAlertDialogue(requireActivity(), model.getString("message"))
                }
            }
            ApiExtentions.ApiDef.SUBMIT_SCHOOL_FORM -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    // Set the adapter to the AutoCompleteTextView
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
                var fieldobject = JSONObject()
                fieldobject.put("is_approved", 0)
                fieldobject.put("rejection_reason", "")
                fieldobject.put("value", uploadImageData.url)
                fieldobject.put("type", "text")
                requestObject.put(
                    getTag(imageList.get(imageUploadPosition-1).fileName!!),
                    fieldobject
                )
                if(imageUploadPosition == imageCount)
                {
                    submitForm()
                }
                else{
                    uploadImage(imageList.get(imageUploadPosition++))
                }

            }

            ApiExtentions.ApiDef.UPLOAD_AUDIO -> {
                val model = JSONObject(o.toString())
                val uploadImageData = Gson().fromJson(
                    model.getJSONObject("data").toString(),
                    UploadImageData::class.java
                )
                var fieldobject = JSONObject()
                fieldobject.put("is_approved", 0)
                fieldobject.put("rejection_reason", "")
                fieldobject.put("value", uploadImageData.url)
                fieldobject.put("type", "audio")
                requestObject.put(
                    "audio_file",
                    fieldobject
                )
                uploadImage(imageList.get(imageUploadPosition++))

            }

            else -> Toast.makeText(
                requireActivity(),
                "Api Not Integrated",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onApiError(message: String?) {
        cancelProgressDialog()
        println(message)
        redirectionAlertDialogue(requireActivity(), message!!)
    }

    override fun retry(type: Int) {

        when (ApiExtentions.ApiDef.entries[type]) {
            else -> Toast.makeText(
                requireActivity(),
                "Api Not Integrated",
                Toast.LENGTH_LONG
            ).show()
        }

    }
}

class BreakException : RuntimeException()

data class ImageLinks(
    var url :String?,
    var fileName : String?
)