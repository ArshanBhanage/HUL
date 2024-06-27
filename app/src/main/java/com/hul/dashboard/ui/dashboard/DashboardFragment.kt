package com.hul.dashboard.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.OnFocusChangeListener
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import com.hul.curriculam.Curriculam
import com.hul.curriculam.ui.schoolCode.SchoolCodeAdapter
import com.hul.dashboard.DashboardComponent
import com.hul.data.Attendencemodel
import com.hul.data.District
import com.hul.data.PerformanceData
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.data.SchoolCode
import com.hul.data.State
import com.hul.data.UploadImageData
import com.hul.databinding.FragmentDashboardBinding
import com.hul.screens.field_auditor_dashboard.ui.school_activity.SchoolActivityFragment
import com.hul.sync.VisitDataTable
import com.hul.sync.VisitDataViewModel
import com.hul.user.UserInfo
import com.hul.utils.ASSIGNED
import com.hul.utils.ConnectionDetector
import com.hul.utils.INITIATED
import com.hul.utils.PARTIALLY_SUBMITTED
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectToLogin
import com.hul.utils.redirectionAlertDialogue
import com.hul.utils.setProgressDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class DashboardFragment : Fragment(), ApiHandler, RetryInterface, DashboardFragmentInterface,
    ListDialogInterface {

    private var binding: FragmentDashboardBinding? = null

    private lateinit var dashboardComponent: DashboardComponent

    @Inject
    lateinit var dashboardViewModel: DashboardViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var visitDataViewModel: VisitDataViewModel

    @Inject
    lateinit var apiController: APIController

    private var imageIndex: Int = 0

    private var isSyncing: Boolean = false

    private var visitDataTableUploading: VisitDataTable? = null

    private var requestModel: RequestModel? = null

    @Inject
    lateinit var uploadFileController: UploadFileController

    var adapter: SchoolCodeAdapter? = null

    var schoolCodes: ArrayList<SchoolCode> = ArrayList()

    var selectedSchoolCode: SchoolCode? = null

    var visitList: ArrayList<ProjectInfo> = ArrayList()

    var projectInfoCompleted = ProjectInfo()

    private var districtList: ArrayList<District> = ArrayList()
    private var stateList: ArrayList<State> = ArrayList()

    var districtCallBack: ListDialogInterface? = null;

    var stateCallBack: ListDialogInterface? = null;

    private var syncDataList: ArrayList<VisitDataTable> = ArrayList()

    private var currentLocation: Location? = null
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest

    private val handler = Handler(Looper.getMainLooper())

    private var isAddSchoolFlow = false

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding!!.root

        binding!!.lifecycleOwner = viewLifecycleOwner
        dashboardComponent =
            (activity?.application as HULApplication).appComponent.dashboardComponent()
                .create()
        dashboardComponent.inject(this)
        binding!!.viewModel = dashboardViewModel

        binding!!.locationToVisit.layoutManager = LinearLayoutManager(context)

        binding!!.todaysVisit.layoutManager = LinearLayoutManager(context)

        binding!!.myArea.text = userInfo.myArea



        binding!!.punchInButton.setOnClickListener {
            redirectToAttendence(ProjectInfo(location_id = "1"))
        }

        binding!!.dayToday.text = dayOfWeek()
        binding!!.date.text = formatDate(Date(), "dd MMM yyyy")
        binding!!.txtLatter.text = userInfo.projectName.trim().split("")[1].uppercase()

        binding!!.rlProfile.setOnClickListener {
            showCustomDialog()
        }

        binding!!.schoolCode.onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                selectedSchoolCode = null
                binding!!.llGetDirection.visibility = GONE
            } else {
                hideKeyboard(view)
            }
        }

        binding!!.schoolCode.setOnItemClickListener { parent, view, position, id ->
            selectedSchoolCode = schoolCodes[position]
            binding!!.llGetDirection.visibility =
                if (selectedSchoolCode!!.lattitude == null) GONE else VISIBLE
            binding!!.schoolCode.setText(selectedSchoolCode!!.external_id1)
            schoolCodes[position].id?.let { getSchoolVisits(it) }
            binding!!.schoolCode.clearFocus()
        }

        binding!!.schoolCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Code to execute before the text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Code to execute when the text is changed

            }

            override fun afterTextChanged(s: Editable?) {
                // Code to execute after the text is changed

                val check = schoolCodes.filter { it.external_id1.equals(s.toString()) }
                if (binding!!.schoolCode.text.isNotEmpty() && check.size == 0 && s.toString().length < 20) {
                    getSchoolCodes(binding!!.schoolCode.text.toString())
                }
            }
        })

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)

                Log.e("Location :: ", locationResult.lastLocation.toString())

                // Normally, you want to save a new location to a database. We are simplifying
                // things a bit and just saving it as a local variable, as we only need it again
                // if a Notification is created (when the user navigates away from app).
                currentLocation = locationResult.lastLocation
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            }
        }

        binding!!.llGetDirection.setOnClickListener {
            if (currentLocation != null && selectedSchoolCode != null) {
                selectedSchoolCode!!.longitude?.let { it1 ->
                    selectedSchoolCode!!.lattitude?.let { it2 ->
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


        if (allPermissionsGranted()) {
            checkLocationSettings()
        } else {
            requestPermission()
        }

        binding!!.btnAddNewSchool.setOnClickListener { showAddSchoolDialog() }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        //visitDataViewModel.insert(VisitDataTable(jsonData = "Nitin", visitNumber = 1, project = "Test", uDiceCode = "retest"))

        binding!!.syncNow.setOnClickListener {
            if (syncDataList.isNotEmpty()) {
                setProgressDialog(requireContext(), "Syncing Data")
                startSync(syncDataList[syncDataList.size - 1])
            }
        }

    }

    private fun fetchVisitData() {
                if (isSyncing) {
                    if (syncDataList.isNotEmpty()) {
                        startSync(syncDataList[syncDataList.size - 1])
                    } else {
                        isSyncing = false
                        cancelProgressDialog()
                        showViewTemporarily(binding!!.llVisitSuccessToast, 2000)
                        getTodaysVisit()
                    }
                }
    }

    private fun startSync(visitDataTable: VisitDataTable) {
        imageIndex = 0
        isSyncing = true
        visitDataTableUploading = visitDataTable
        requestModel = Gson().fromJson(visitDataTable.jsonData, RequestModel::class.java)
        uploadImage(requestModel!!.visitData!!.visit_image_1!!.value.toString().toUri())
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

    fun submitForm(requestModel: RequestModel) {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            //setProgressDialog(requireContext(), "Loading Leads")
            apiController.getApiResponse(
                this,
                requestModel,
                ApiExtentions.ApiDef.VISIT_DATA.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.VISIT_DATA.ordinal, this)
        }

    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    private fun getPerformanceModel(): RequestModel {
        return RequestModel(

        )
    }

    private fun getPerformance() {
        apiController.getApiResponse(
            this,
            getPerformanceModel(),
            ApiExtentions.ApiDef.GET_PERFORMANCE.ordinal
        )
    }

    private fun getTodaysVisitModel(): RequestModel {
        return RequestModel(

        )
    }

    private fun getTodaysVisit() {
        apiController.getApiResponse(
            this,
            getTodaysVisitModel(),
            ApiExtentions.ApiDef.VISIT_LIST_BY_STATUS.ordinal
        )
    }

    // Function to hide the keyboard
    private fun hideKeyboard(view: View) {
        val imm =
            requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun getSchoolCodes(s: String) {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            //setProgressDialog(requireContext(), "Loading Leads")
            apiController.getApiResponse(
                this,
                getSchoolCodesModel(s),
                ApiExtentions.ApiDef.SCHOOL_CODES.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.SCHOOL_CODES.ordinal, this)
        }

    }

    private fun showDistrictDialog(districtList: ArrayList<District>) {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
        val inflater = getLayoutInflater()
        val dialogView: View = inflater.inflate(R.layout.list_dialog, null)
        builder.setView(dialogView)
        val alertDialog: android.app.AlertDialog = builder.create()

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView);
        val adapter: DistrictsAdapter =
            DistrictsAdapter(districtList, object : ListDialogInterface {
                override fun onDistrictSelect(district: District) {
                    districtCallBack?.onDistrictSelect(district)
                    alertDialog.dismiss()
                }

                override fun onStateSelect(state: State) {

                }
            }, requireActivity())

        recyclerView.adapter = adapter

        alertDialog.show()
    }

    private fun showStateDialog(stateList: ArrayList<State>) {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
        val inflater = getLayoutInflater()
        val dialogView: View = inflater.inflate(R.layout.list_dialog, null)
        builder.setView(dialogView)
        val alertDialog: android.app.AlertDialog = builder.create()

        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.recyclerView);
        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle);

        dialogTitle.text = "Select State"

        val adapter: StateAdapter =
            StateAdapter(stateList, object : ListDialogInterface {
                override fun onDistrictSelect(district: District) {

                }

                override fun onStateSelect(state: State) {
                    stateCallBack?.onStateSelect(state)
                    alertDialog.dismiss()
                }
            }, requireActivity())

        recyclerView.adapter = adapter

        alertDialog.show()
    }

    private fun getSchoolCodesModel(s: String): RequestModel {
        return RequestModel(
            projectId = userInfo.projectId,
            externalId = s
        )
    }

    private fun getSchoolVisits(schoolId: Int) {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            //setProgressDialog(requireContext(), "Loading Leads")
            apiController.getApiResponse(
                this,
                getSVisitsBySchoolCode(schoolId),
                ApiExtentions.ApiDef.VISIT_LIST_BY_SCHOOL_CODE.ordinal
            )
        } else {
            noInternetDialogue(
                requireContext(),
                ApiExtentions.ApiDef.VISIT_LIST_BY_SCHOOL_CODE.ordinal,
                this
            )
        }

    }

    private fun getSchoolVisitsCompleted(schoolId: Int) {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            //setProgressDialog(requireContext(), "Loading Leads")
            apiController.getApiResponse(
                this,
                getSVisitsBySchoolCode(schoolId),
                ApiExtentions.ApiDef.VISIT_LIST_BY_SCHOOL_CODE_Completed.ordinal
            )
        } else {
            noInternetDialogue(
                requireContext(),
                ApiExtentions.ApiDef.VISIT_LIST_BY_SCHOOL_CODE_Completed.ordinal,
                this
            )
        }

    }

    private fun getDistrictList(callBack: ListDialogInterface) {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            districtCallBack = callBack
            apiController.getApiResponse(
                this,
                getDistricts(userInfo.projectId),
                ApiExtentions.ApiDef.GET_DISTRICTS.ordinal
            )
        } else {
            noInternetDialogue(
                requireContext(),
                ApiExtentions.ApiDef.GET_DISTRICTS.ordinal,
                this
            )
        }

    }

    private fun getStateList(callBack: ListDialogInterface) {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            stateCallBack = callBack
            apiController.getApiResponse(
                this,
                getDistricts(userInfo.projectId),
                ApiExtentions.ApiDef.GET_STATES.ordinal
            )
        } else {
            noInternetDialogue(
                requireContext(),
                ApiExtentions.ApiDef.GET_STATES.ordinal,
                this
            )
        }

    }


    private fun getSVisitsBySchoolCode(id: Int): RequestModel {
        return RequestModel(
            schoolId = id,
        )
    }

    private fun getDistricts(id: String): RequestModel {
        return RequestModel(
            projectId = id,
        )
    }

    private fun addVisit(id: String, visitNumber: String) {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            //setProgressDialog(requireContext(), "Loading Leads")
            apiController.getApiResponse(
                this,
                addVisitModel(id, visitNumber),
                ApiExtentions.ApiDef.ADD_VISIT.ordinal
            )
        } else {
            noInternetDialogue(
                requireContext(),
                ApiExtentions.ApiDef.ADD_VISIT.ordinal,
                this
            )
        }

    }

    private fun addVisitModel(id: String, visitNumber: String): RequestModel {
        return RequestModel(
            location_id = id,
            visit_number = visitNumber
        )
    }

    private fun formatDate(date: Date, format: String): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.format(date)
    }

    private fun dayOfWeek(): String {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        return when (dayOfWeek) {
            Calendar.SUNDAY -> "SUNDAY"
            Calendar.MONDAY -> "MONDAY"
            Calendar.TUESDAY -> "TUESDAY"
            Calendar.WEDNESDAY -> "WEDNESDAY"
            Calendar.THURSDAY -> "THURSDAY"
            Calendar.FRIDAY -> "FRIDAY"
            Calendar.SATURDAY -> "SATURDAY"
            else -> "UNKNOWN"
        }
    }

    fun getDayOfWeek(): String {
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        return when (dayOfWeek) {
            Calendar.SUNDAY -> "SUNDAY"
            Calendar.MONDAY -> "MONDAY"
            Calendar.TUESDAY -> "TUESDAY"
            Calendar.WEDNESDAY -> "WEDNESDAY"
            Calendar.THURSDAY -> "THURSDAY"
            Calendar.FRIDAY -> "FRIDAY"
            Calendar.SATURDAY -> "SATURDAY"
            else -> "UNKNOWN"
        }
    }

    private fun showCustomDialog() {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
        val inflater = getLayoutInflater()
        val dialogView: View = inflater.inflate(R.layout.profile_dialog, null)
        builder.setView(dialogView)
        val alertDialog: android.app.AlertDialog = builder.create()

        val llLogout = dialogView.findViewById<LinearLayout>(R.id.llLogOut);
        val txtMobiliserName = dialogView.findViewById<TextView>(R.id.txtMobiliserName)
        txtMobiliserName.text = userInfo.projectName

        llLogout.setOnClickListener {
            alertDialog.dismiss()
            userInfo.authToken = ""
            redirectToLogin(requireContext())
        }

        alertDialog.show()
    }

    private fun showAddSchoolDialog() {
        val builder: android.app.AlertDialog.Builder = android.app.AlertDialog.Builder(context)
        val inflater = getLayoutInflater()
        val dialogView: View = inflater.inflate(R.layout.add_school_dialog, null)
        builder.setView(dialogView)
        val alertDialog: android.app.AlertDialog = builder.create()

        alertDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        val btnClose = dialogView.findViewById<AppCompatButton>(R.id.btnClose);
        val btnAdd = dialogView.findViewById<AppCompatButton>(R.id.btnAdd);

        val edtSchoolCode = dialogView.findViewById<TextInputEditText>(R.id.edtSchoolCode);
        val edtSchoolName = dialogView.findViewById<TextInputEditText>(R.id.edtSchoolName);
        val edtWardBlock = dialogView.findViewById<TextInputEditText>(R.id.edtWardBlock);
        val edtState = dialogView.findViewById<TextInputEditText>(R.id.edtState);
        val edtDistrict = dialogView.findViewById<TextInputEditText>(R.id.edtDistrict);

        var districtToSubmit: District? = null
        var stateToSubmit: State? = null

        edtDistrict.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                getDistrictList(object : ListDialogInterface {
                    override fun onDistrictSelect(district: District) {
                        edtDistrict.setText(district.area_name)
                        districtToSubmit = district
                    }

                    override fun onStateSelect(state: State) {

                    }
                })
            }
        }

        edtState.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                getStateList(object : ListDialogInterface {
                    override fun onDistrictSelect(district: District) {

                    }

                    override fun onStateSelect(state: State) {
                        edtState.setText(state.location_state)
                        stateToSubmit = state
                    }
                })
            }
        }

        btnAdd.setOnClickListener {

            val schoolCode = edtSchoolCode.text.toString()
            val schoolName = edtSchoolName.text.toString()
            val wardBlock = edtWardBlock.text.toString()

            if (schoolCode.isEmpty() || schoolName.isEmpty() || wardBlock.isEmpty()
                || stateToSubmit == null || districtToSubmit == null
            ) {
                Toast.makeText(requireContext(), "Please fill all inputs", Toast.LENGTH_LONG).show()
            } else if (!isValidSchoolCode(schoolCode)) {
                Toast.makeText(
                    requireContext(),
                    "School code can only be alpha-numeric value",
                    Toast.LENGTH_LONG
                ).show()
            } else {

                alertDialog.dismiss()

                val addSchoolModel = RequestModel(
                    location_name = schoolName,
                    area_id = districtToSubmit!!.area_id.toString(),
                    project_id = userInfo.projectId,
                    location_type = "School",
                    lattitude = currentLocation?.latitude.toString(),
                    longitude = currentLocation?.longitude.toString(),
                    external_id1 = schoolCode,
                    external_id1_description = "UDISE Code",
                    external_id2 = null,
                    external_id2_description = "Temp Code",
                    location_ward = wardBlock,
                    location_district = districtToSubmit!!.area_name,
                    location_state = stateToSubmit!!.location_state,
                    remarks = ""
                )

                apiController.getApiResponse(
                    this,
                    addSchoolModel,
                    ApiExtentions.ApiDef.ADD_NEW_SCHOOL.ordinal
                )
            }
        }

        btnClose.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }

    fun updateLocalList()
    {
        visitDataViewModel.allSyncData.observe(requireActivity()) { visitDataList ->
            syncDataList = ArrayList(visitDataList)
            binding!!.visitsLeft.text =
                visitDataList.size.toString() + " " + requireActivity().getString(R.string.todays_visit_left)
        }
    }

    override fun onResume() {
        super.onResume()
        updateLocalList()
        loadLocations()

        binding!!.schoolCode.setText("")
        binding!!.txtProfileName.text = "Hi, " + userInfo.projectName


    }

    private fun loadLocations() {

//        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
//            setProgressDialog(requireContext(), "Loading Leads")
//            apiController.getApiResponse(
//                this,
//                loadLocationsModel(),
//                ApiExtentions.ApiDef.VISIT_LIST.ordinal
//            )
//        } else {
//            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.VISIT_LIST.ordinal, this)
//        }

        if (dashboardViewModel.attendenceToday.value == null) {
            getAttendence()
        }

    }

    private fun loadLocationsModel(): RequestModel {
        return RequestModel(
            projectId = userInfo.projectId
        )
    }

    private fun getAttendence() {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            apiController.getApiResponse(
                this,
                getAttendenceModel(),
                ApiExtentions.ApiDef.GET_ATTENDENCE.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.GET_ATTENDENCE.ordinal, this)
        }

    }

    private fun getAttendenceModel(): RequestModel {
        return RequestModel(
            projectId = userInfo.projectId
        )
    }

    override fun onApiSuccess(o: String?, objectType: Int) {


        when (ApiExtentions.ApiDef.entries[objectType]) {

            ApiExtentions.ApiDef.ADD_NEW_SCHOOL -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    Toast.makeText(requireContext(), "School added successfully", Toast.LENGTH_LONG)
                        .show()
                    selectedSchoolCode = SchoolCode(id = model.getInt("data"));
                    binding!!.llGetDirection.visibility = GONE
                    isAddSchoolFlow = true;
//                    getSchoolVisits(model.getInt("data")) // Temp remove
                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }
            }

            ApiExtentions.ApiDef.GET_STATES -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<State?>?>() {}.type
                    stateList =
                        Gson().fromJson(model.getJSONArray("data").toString(), listType);
                    showStateDialog(stateList);
                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }
            }

            ApiExtentions.ApiDef.GET_DISTRICTS -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<District?>?>() {}.type
                    districtList =
                        Gson().fromJson(model.getJSONArray("data").toString(), listType);
                    showDistrictDialog(districtList);
                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }
            }

            ApiExtentions.ApiDef.GET_ATTENDENCE -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<Attendencemodel?>?>() {}.type
                    val items: ArrayList<Attendencemodel> =
                        Gson().fromJson(model.getJSONArray("data").toString(), listType);
                    val currentObject = items.get(items.size - 1)
                    dashboardViewModel.attendenceToday.value = currentObject
                    try {
                        items.removeAt(items.size - 1)
                        // Remove the first element
                        items.removeAt(0)
                    } catch (e: Exception) {
                        Log.e(DashboardFragment::class.qualifiedName, "onApiSuccess: " + e.message)
                    }
                    val adapter = AttendenceAdapter(requireContext(), items)
                    binding!!.gridView.adapter = adapter
                    if (currentObject.date != null && currentObject.date?.length!! > 10) {

                        binding!!.time.text = currentObject.date!!.substring(
                            11,
                            currentObject.date!!.length
                        )
                    }

                    if (currentObject.present != null && currentObject.present!!) {
                        binding!!.punchInButton.visibility = GONE
                        binding!!.punchInButtonDisabled.visibility = VISIBLE
                        binding!!.punchInButton.isEnabled = false
                    } else {
                        binding!!.punchInButton.visibility = VISIBLE
                    }

                    getPerformance()

                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }

            }

            ApiExtentions.ApiDef.GET_PERFORMANCE -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    try {
                        val performanceData = Gson().fromJson(
                            model.getJSONObject("data").toString(),
                            PerformanceData::class.java
                        )
                        binding!!.txtVisits.text = performanceData.till_date.total_visits.toString()
                        binding!!.txtAttendance.text =
                            performanceData.till_date.attendance.toString() + "%"
                        binding!!.txtTotalVisits.text =
                            performanceData.till_date.audit_approval.toString() + "%"
                    } catch (_: Exception) {

                    }

                    getTodaysVisit()
                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }
            }

            ApiExtentions.ApiDef.SCHOOL_CODES -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<SchoolCode?>?>() {}.type
                    schoolCodes =
                        Gson().fromJson(model.getJSONArray("data").toString(), listType);
                    if (schoolCodes.size > 0) {
                        selectedSchoolCode = schoolCodes[0]
                        binding!!.llGetDirection.visibility =
                            if (selectedSchoolCode!!.lattitude == null) GONE else VISIBLE
                    } else {
                        selectedSchoolCode = SchoolCode(-1)
                        selectedSchoolCode?.external_id1 = "Add New"
                        schoolCodes.add(selectedSchoolCode!!)
                    }

                    adapter = SchoolCodeAdapter(
                        requireContext(),
                        R.layout.school_code_dropdown,
                        schoolCodes
                    )

                    // Set the adapter to the AutoCompleteTextView
                    binding!!.schoolCode.setAdapter(adapter)
                    binding!!.schoolCode.requestFocus()
                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }

            }

            ApiExtentions.ApiDef.ADD_VISIT -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    selectedSchoolCode?.id?.let { getSchoolVisits(it) }
                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }
            }

            ApiExtentions.ApiDef.VISIT_LIST_BY_SCHOOL_CODE -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())

                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<ProjectInfo?>?>() {}.type

                    visitList =
                        Gson().fromJson(model.getJSONArray("data").toString(), listType);

                    val pendingVisits: ArrayList<ProjectInfo> = ArrayList()

                    var flag = true

                    for (project in visitList) {
                        if (project.visit_status.equals(ASSIGNED, ignoreCase = true)
                            || project.visit_status.equals(INITIATED, ignoreCase = true)
                            || project.visit_status.equals(PARTIALLY_SUBMITTED, ignoreCase = true)
                        ) {
                            flag = false
                            project.let { pendingVisits.add(it) }
                        }
                    }

                    if (isAddSchoolFlow && visitList.size > 0) {
                        isAddSchoolFlow = false
                        redirectToAttendence(visitList[0])
                    } else if (flag && visitList.size < 3) {
                        addVisit(
                            selectedSchoolCode!!.id.toString(),
                            (visitList.size + 1).toString()
                        )
                    }

                    val sortedList = visitList.sortedBy { it.visit_number }

                    val listToShowInAdapter: ArrayList<ProjectInfo> = ArrayList();
                    if (sortedList.isNotEmpty()) {
                        listToShowInAdapter.add(sortedList.last())
                    }

                    val myVisitsAdapter =
                        MyVisitsAdapter(listToShowInAdapter, this, requireContext())
                    // Setting the Adapter with the recyclerview
                    binding!!.locationToVisit.adapter = myVisitsAdapter
                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }

            }

            ApiExtentions.ApiDef.VISIT_LIST_BY_SCHOOL_CODE_Completed -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())

                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<ProjectInfo?>?>() {}.type

                    visitList =
                        Gson().fromJson(model.getJSONArray("data").toString(), listType);
                    visitList.forEachIndexed{index ,item ->
                        visitList[index].visit_status = "SUBMITTED"
                    }
                    redirectToCompleted(projectInfoCompleted)

                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }

            }

            ApiExtentions.ApiDef.VISIT_LIST_BY_STATUS -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<ProjectInfo?>?>() {}.type
                    val visitListFromBE: ArrayList<ProjectInfo> =
                        Gson().fromJson(model.getJSONArray("data").toString(), listType);



                    if (visitListFromBE.size > 0 || syncDataList.isNotEmpty()) {

                        // Change visit total dynamically


                        if (syncDataList.isNotEmpty()) {
                            for (data in syncDataList) {
                                val projectInfo = ProjectInfo(
                                    visit_number = data.visitNumber.toString(),
                                    location_name = data.locationName,
                                    visit_status = "SUBMITTED",
                                    location_id = data.locationId,
                                    localString = data.jsonData
                                )
                                visitListFromBE.add(projectInfo)
                            }
                        }

                        val myVisitsAdapter =
                            MyVisitsAdapter(visitListFromBE, this, requireContext())

                        // Setting the Adapter with the recyclerview
                        binding!!.todaysVisit.adapter = myVisitsAdapter
                    } else {
                        binding!!.todaysVisitParent.visibility = View.GONE
                    }

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
                    requestModel!!.visitData!!.visit_image_1!!.value = uploadImageData.url
                    uploadImage(requestModel!!.visitData!!.visit_image_2!!.value.toString().toUri())
                } else if (uploadImageData != null && imageIndex == 1) {
                    imageIndex += 1;
                    requestModel!!.visitData!!.visit_image_2!!.value = uploadImageData.url
                    uploadImage(requestModel!!.visitData!!.visit_image_3!!.value.toString().toUri())
                } else if (uploadImageData != null && imageIndex == 2) {
                    imageIndex += 1;
                    requestModel!!.visitData!!.visit_image_3!!.value = uploadImageData.url
                    uploadImage(requestModel!!.visitData!!.visit_image_4!!.value.toString().toUri())
                } else if (uploadImageData != null && imageIndex == 3) {
                    imageIndex += 1;
                    requestModel!!.visitData!!.visit_image_4!!.value = uploadImageData.url
                    submitForm(requestModel!!)
                }
            }

            ApiExtentions.ApiDef.VISIT_DATA -> {
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
//                    userInfo.didUserSubmitNewVisit = true
//                    val intent = Intent(activity, Dashboard::class.java)
//                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
//                    startActivity(intent)
//                    requireActivity().finish()
                    visitDataViewModel.deleteById(visitDataTableUploading!!.id)
                    syncDataList.removeIf { it.id == visitDataTableUploading!!.id }
                    fetchVisitData()


                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }
            }

            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }
    }

    override fun onApiError(message: String?) {
        if (message?.equals(context?.getString(R.string.session_expire))!!) {
            userInfo.authToken = ""
            redirectionAlertDialogue(requireContext(), message)
        } else {
            redirectionAlertDialogue(requireContext(), message)
        }
    }

    override fun retry(type: Int) {

        when (ApiExtentions.ApiDef.entries[type]) {
            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }

    }

    override fun redirectToAttendence(projectInfo: ProjectInfo) {

            if (dashboardViewModel.attendenceToday.value?.present == true) {

                if (projectInfo.visit_status.equals("SUBMITTED", true)) {
                    getSchoolVisitsCompleted(projectInfo.location_id!!.toInt())
                    projectInfoCompleted = projectInfo
                    return
                }

                val bundle = Bundle()
                var uDiceCode = ""
                uDiceCode = if (selectedSchoolCode?.external_id1 != null) {
                    selectedSchoolCode?.external_id1!!
                } else {
                    selectedSchoolCode?.external_id2.toString()
                }
                bundle.putString(
                    "uDiceCode",
                    uDiceCode
                )
                bundle.putString(
                    "schoolInformation",
                    Gson().toJson(selectedSchoolCode)
                )
                bundle.putString(
                    "visitList",
                    Gson().toJson(visitList)
                )
                bundle.putString("localData", projectInfo.localString)
                findNavController().navigate(
                    R.id.action_schoolCodeFragment_to_schoolFormFragment,
                    bundle
                )
            } else {
                val bundle = Bundle()
                bundle.putString("projectInfo", Gson().toJson(projectInfo))
                findNavController().navigate(
                    R.id.action_dashboardFragment_to_attendenceFragment,
                    bundle
                )
            }

    }

    fun redirectToCompleted(projectInfo: ProjectInfo) {


            val bundle = Bundle()
            var uDiceCode = ""
            uDiceCode = if (selectedSchoolCode?.external_id1 != null) {
                selectedSchoolCode?.external_id1!!
            } else {
                selectedSchoolCode?.external_id2.toString()
            }
            bundle.putString(
                "uDiceCode",
                uDiceCode
            )
            bundle.putString(
                "schoolInformation",
                Gson().toJson(selectedSchoolCode)
            )
            bundle.putString(
                "visitList",
                Gson().toJson(visitList)
            )
            bundle.putString("localData", projectInfo.localString)
            findNavController().navigate(
                R.id.action_schoolCodeFragment_to_schoolFormFragment,
                bundle
            )
        visitList = ArrayList()
        projectInfoCompleted = ProjectInfo()

    }



    /*override fun redirectToAttendence(projectInfo: ProjectInfo) {

        if (dashboardViewModel.attendenceToday.value!!.present!!) {
            redirectToCurriculam(projectInfo)
        } else {
            val bundle = Bundle()
            bundle.putString("projectInfo", Gson().toJson(projectInfo))
            findNavController().navigate(
                R.id.action_dashboardFragment_to_attendenceFragment,
                bundle
            )
        }
    }*/

    private fun redirectToCurriculam(projectInfo: ProjectInfo) {
        val intent = Intent(activity, Curriculam::class.java)
        intent.putExtra("projectInfo", Gson().toJson(projectInfo))
        startActivity(intent)
    }

    override fun onDistrictSelect(district: District) {

    }

    override fun onStateSelect(state: State) {

    }

    private fun requestLocation() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        requestLocation.launch(intent)
    }

    private fun requestPermission() {
        requestPermission.launch(SchoolActivityFragment.REQUIRED_PERMISSIONS)
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

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
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


    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

    }

    private fun isValidSchoolCode(input: String): Boolean {
        // Regular expression to check if the input is alphanumeric and
        // does not contain spaces or special characters
        val regex = "^[a-zA-Z0-9]*$".toRegex()
        return input.matches(regex)
    }

    private fun showViewTemporarily(view: View, duration: Long) {
        if (userInfo.didUserSubmitNewVisit) {
            view.visibility = View.VISIBLE
            handler.postDelayed({
                userInfo.didUserSubmitNewVisit = false
                view.visibility = View.GONE
            }, duration)
        }
    }
}