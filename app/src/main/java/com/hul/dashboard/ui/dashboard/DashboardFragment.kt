package com.hul.dashboard.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
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
import com.hul.dashboard.Dashboard
import com.hul.dashboard.DashboardComponent
import com.hul.data.Attendencemodel
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.databinding.FragmentDashboardBinding
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import com.hul.utils.setProgressDialog
import org.json.JSONObject
import java.lang.reflect.Type
import java.text.SimpleDateFormat
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
//        val adapter = ArrayAdapter(
//            requireContext(),
//            android.R.layout.simple_spinner_item, resources.getStringArray(com.hul.R.array.status)
//        )
//        binding.autocomplete.setAdapter(adapter)

        var visitList = arrayListOf("Location 1", "Location 2", "Location 3")
        binding.locationToVisit.layoutManager = LinearLayoutManager(context)


//        binding.visitedLocation.layoutManager = LinearLayoutManager(context)
//        val visitedLocationAdapter = VisitedLocationAdapter(visitList, this, requireContext())
//
//        // Setting the Adapter with the recyclerview
//        binding.visitedLocation.adapter = visitedLocationAdapter

        binding.myArea.text = userInfo.myArea


        binding.punchInButton.setOnClickListener {
            redirectToAttendence(ProjectInfo(location_id = "1"))
        }

        binding.date.text = formatDate(Date(), "dd MMM yyyy")

        return root
    }

    fun formatDate(date: Date, format: String): String {
        val dateFormat = SimpleDateFormat(format, Locale.getDefault())
        return dateFormat.format(date)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()

        loadLocations()
    }

    fun loadLocations() {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            setProgressDialog(requireContext(), "Loading Leads")
            apiController.getApiResponse(
                this,
                loadLocationsModel(),
                ApiExtentions.ApiDef.VISIT_LIST.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.VISIT_LIST.ordinal, this)
        }

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
        when (ApiExtentions.ApiDef.values()[objectType]) {

            ApiExtentions.ApiDef.VISIT_LIST -> {
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<ProjectInfo?>?>() {}.type
                    var projectInfo: ArrayList<ProjectInfo> =
                        Gson().fromJson(model.getJSONArray("data").toString(), listType);

                    val myVisitsAdapter = MyVisitsAdapter(projectInfo, this, requireContext())

                    // Setting the Adapter with the recyclerview
                    binding.visitNumbers.text =
                        projectInfo.size.toString() + " " + requireContext().getString(R.string.visit_number)
                    binding.locationToVisit.adapter = myVisitsAdapter
                    getAttendence()

                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }

            }

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
                        binding.punchInButton.visibility = View.GONE
                    } else {
                        binding.punchInButton.visibility = View.VISIBLE
                    }

                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }

            }

            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }
    }

    override fun onApiError(message: String?) {
        redirectionAlertDialogue(requireContext(), message!!)
    }

    override fun retry(type: Int) {

        when (ApiExtentions.ApiDef.values()[type]) {
            ApiExtentions.ApiDef.VISIT_LIST -> loadLocations()
            ApiExtentions.ApiDef.GET_ATTENDENCE -> getAttendence()
            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }

    }

    override fun redirectToAttendence(projectInfo: ProjectInfo) {

        if (dashboardViewModel.attendenceToday.value!!.present!!) {
            redirectToCurriculam(projectInfo)
        } else {
            var bundle = Bundle()
            bundle.putString("projectInfo", Gson().toJson(projectInfo))
            findNavController().navigate(
                R.id.action_dashboardFragment_to_attendenceFragment,
                bundle
            )
        }
    }

    private fun redirectToCurriculam(projectInfo: ProjectInfo) {
        val intent = Intent(activity, Curriculam::class.java)
        intent.putExtra("projectInfo",Gson().toJson(projectInfo))
        startActivity(intent)
    }
}