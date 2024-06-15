package com.hul.dashboard.ui.dashboard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hul.HULApplication
import com.hul.R
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.controller.APIController
import com.hul.curriculam.Curriculam
import com.hul.curriculam.ui.schoolCode.SchoolCodeAdapter
import com.hul.dashboard.DashboardComponent
import com.hul.data.Attendencemodel
import com.hul.data.PerformanceData
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.data.SchoolCode
import com.hul.databinding.FragmentDashboardBinding
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectToLogin
import com.hul.utils.redirectionAlertDialogue
import org.json.JSONObject
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject


class DashboardFragment : Fragment(), ApiHandler, RetryInterface, DashboardFragmentInterface {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var dashboardComponent: DashboardComponent

    @Inject
    lateinit var dashboardViewModel: DashboardViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    var adapter: SchoolCodeAdapter? = null

    var schoolCodes: ArrayList<SchoolCode> = ArrayList()

    var selectedSchoolCode: SchoolCode? = null

    var visitList: ArrayList<ProjectInfo> = ArrayList()

    var uDiceCode: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        dashboardComponent =
            (activity?.application as HULApplication).appComponent.dashboardComponent()
                .create()
        dashboardComponent.inject(this)
        binding.viewModel = dashboardViewModel

        binding.locationToVisit.layoutManager = LinearLayoutManager(context)

        binding.myArea.text = userInfo.myArea


        binding.punchInButton.setOnClickListener {
            redirectToAttendence(ProjectInfo(location_id = "1"))
        }

        binding.dayToday.text = getCurrentDayOfWeek()
        binding.date.text = formatDate(Date(), "dd MMM yyyy")
        binding.txtLatter.text = userInfo.projectName.trim().split("")[1].uppercase()

        binding.rlProfile.setOnClickListener {
            showCustomDialog()
        }

        binding.schoolCode.onFocusChangeListener = OnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                selectedSchoolCode = null
            } else {
                hideKeyboard(view)
            }
        }

        binding.schoolCode.setOnItemClickListener { parent, view, position, id ->
            selectedSchoolCode = schoolCodes[position]
            binding.schoolCode.setText(selectedSchoolCode!!.external_id1)
            schoolCodes[position].id?.let { getSchoolVisits(it) }
            binding.schoolCode.clearFocus()
        }

        binding.schoolCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Code to execute before the text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Code to execute when the text is changed

            }

            override fun afterTextChanged(s: Editable?) {
                // Code to execute after the text is changed
                if (!binding.schoolCode.text.isEmpty() && s.toString().length < 10) {
                    getSchoolCodes(binding.schoolCode.text.toString())
                }
            }
        })



        return root
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

    private fun getSchoolCodesModel(s: String): RequestModel {
        return RequestModel(
            projectId = userInfo.projectId,
            externalId = s
        )
    }

    fun getSchoolVisits(schoolId: Int) {

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


    private fun getSVisitsBySchoolCode(id: Int): RequestModel {
        return RequestModel(
            schoolId = id,
        )
    }

    fun addVisit(id: String, visitNumber: String) {

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

    fun formatDate(date: Date, format: String): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.format(date)
    }

    fun getCurrentDayOfWeek(): String {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("EEEE", Locale.getDefault())
        return dateFormat.format(calendar.time)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        loadLocations()

        binding.schoolCode.setText("")
        getAttendence()
    }

    fun loadLocations() {

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

    }

    private fun loadLocationsModel(): RequestModel {
        return RequestModel(
            projectId = userInfo.projectId
        )
    }

    fun getAttendence() {

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

        cancelProgressDialog()
        when (ApiExtentions.ApiDef.entries[objectType]) {

            ApiExtentions.ApiDef.GET_ATTENDENCE -> {
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<Attendencemodel?>?>() {}.type
                    val items: ArrayList<Attendencemodel> =
                        Gson().fromJson(model.getJSONArray("data").toString(), listType);
                    val currentObject = items.get(items.size - 1)
                    dashboardViewModel.attendenceToday.value = currentObject
                    items.removeAt(items.size - 1)
                    // Remove the first element
                    items.removeAt(0)
                    val adapter = AttendenceAdapter(requireContext(), items)
                    binding.gridView.adapter = adapter
                    if (currentObject.date!!.length > 10) {

                        binding.time.text = currentObject.date!!.substring(
                            11,
                            currentObject.date!!.length
                        )
                    }

                    if (currentObject.present!!) {
                        if (currentObject.present!!) {
                            binding.punchInButton.visibility = View.GONE
                            binding.punchInButtonDisabled.visibility = View.VISIBLE
                            binding.punchInButton.isEnabled = false
                        } else {
                            binding.punchInButton.visibility = View.VISIBLE
                        }
                    } else {
                        binding.punchInButton.visibility = View.VISIBLE
                    }

                    getPerformance()

                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }

            }

            ApiExtentions.ApiDef.GET_PERFORMANCE -> {
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    try {
                        val performanceData = Gson().fromJson(
                            model.getJSONObject("data").toString(),
                            PerformanceData::class.java
                        )
                        binding.txtVisits.text = performanceData.till_date.total_visits.toString()
                        binding.txtAttendance.text =
                            performanceData.till_date.attendance.toString() + "%"
                        binding.txtTotalVisits.text =
                            performanceData.till_date.audit_approval.toString() + "%"
                    } catch (e: Exception) {

                    }
                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }
            }

            ApiExtentions.ApiDef.SCHOOL_CODES -> {
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<SchoolCode?>?>() {}.type
                    schoolCodes =
                        Gson().fromJson(model.getJSONArray("data").toString(), listType);

                    adapter = SchoolCodeAdapter(
                        requireContext(),
                        R.layout.school_code_dropdown,
                        schoolCodes
                    )

                    if (schoolCodes.isNotEmpty()) {
                        uDiceCode = schoolCodes.get(0).external_id1
                    }

                    // Set the adapter to the AutoCompleteTextView
                    binding.schoolCode.setAdapter(adapter)
                    binding.schoolCode.requestFocus()
                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }

            }

            ApiExtentions.ApiDef.ADD_VISIT -> {
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    getSchoolVisits(selectedSchoolCode!!.id!!)
                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }

            }

            ApiExtentions.ApiDef.VISIT_LIST, ApiExtentions.ApiDef.VISIT_LIST_BY_SCHOOL_CODE -> {
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<ProjectInfo?>?>() {}.type
                    visitList =
                        Gson().fromJson(model.getJSONArray("data").toString(), listType);

                    val pendingVisits: ArrayList<ProjectInfo> = ArrayList()

                    var flag = true

                    for (project in visitList) {
                        if (project.visit_status.equals("ASSIGNED", ignoreCase = true)
                            || project.visit_status.equals("INITIATED", ignoreCase = true)
                        ) {
                            flag = false
                            pendingVisits.add(project)
                        }
                    }

                    if (flag && visitList.size < 3) {
                        addVisit(
                            selectedSchoolCode!!.id.toString(),
                            (visitList.size + 1).toString()
                        )
                    }

                    val myVisitsAdapter = MyVisitsAdapter(pendingVisits, this, requireContext())

                    // Setting the Adapter with the recyclerview
                    binding.visitNumbers.text =
                        visitList.size.toString() + " " + requireContext().getString(R.string.visit_number)
                    binding.locationToVisit.adapter = myVisitsAdapter

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
            redirectionAlertDialogue(requireContext(), message!!)
        } else {
            redirectionAlertDialogue(requireContext(), message!!)
        }
    }

    override fun retry(type: Int) {

        when (ApiExtentions.ApiDef.values()[type]) {
            ApiExtentions.ApiDef.GET_ATTENDENCE -> getAttendence()
            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }

    }

    override fun redirectToAttendence(projectInfo: ProjectInfo) {

        if (dashboardViewModel.attendenceToday.value!!.present!!) {
            val bundle = Bundle()
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
}