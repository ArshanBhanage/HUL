package com.hul.screens.field_auditor_dashboard.ui.dashboard

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
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
import com.hul.data.Attendencemodel
import com.hul.data.MappedUser
import com.hul.data.PerformanceData
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.data.ResponseModel
import com.hul.data.UserDetails
import com.hul.databinding.FragmentDashboardAuditorBinding
import com.hul.screens.field_auditor_dashboard.FieldAuditorDashboardComponent
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import org.json.JSONObject
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject


class DashboardFragment : Fragment(), ApiHandler, RetryInterface, DashboardFragmentInterface {

    private var _binding: FragmentDashboardAuditorBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var dashboardComponent: FieldAuditorDashboardComponent

    @Inject
    lateinit var dashboardViewModel: DashboardViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    val mobiliserUsers = mutableListOf<MappedUser>()

    lateinit var mobilisersAdapter: MobilisersAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDashboardAuditorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        dashboardComponent =
            (activity?.application as HULApplication).appComponent.fieldAuditorDashboardComponent()
                .create()
        dashboardComponent.inject(this)
        binding.viewModel = dashboardViewModel

        binding.recyclerViewMobilisers.layoutManager = LinearLayoutManager(context)

        binding.myArea.text = userInfo.myArea

        binding.punchInButton.setOnClickListener {
            redirectToAttendence(ProjectInfo(location_id = "1"))
        }

        binding.date.text = formatDate(Date(), "dd MMM yyyy")

        binding.tillDateButton.setOnClickListener {
            showOptionsDialog()
        }

        binding.txtLatter.text = userInfo.projectName.trim().split("")[1].uppercase()

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
        mobiliserUsers.clear()
        getLogo()
    }

    private fun getLogo() {
        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            apiController.getApiResponse(
                this,
                RequestModel(projectId = userInfo.projectId),
                ApiExtentions.ApiDef.GET_LOGO.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.GET_LOGO.ordinal, this)
        }
    }

    private fun getMobilisers() {
        apiController.getApiResponse(
            this,
            getUserDetailsModel(),
            ApiExtentions.ApiDef.GET_USER_DETAILS.ordinal
        )
    }

    private fun getPerformance() {
        apiController.getApiResponse(
            this,
            getPerformanceModel(),
            ApiExtentions.ApiDef.GET_PERFORMANCE.ordinal
        )
    }



    private fun getUserDetailsModel(): RequestModel {
        return RequestModel()
    }

    private fun getAttendance() {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            apiController.getApiResponse(
                this,
                getAttendanceModel(),
                ApiExtentions.ApiDef.GET_ATTENDENCE.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.GET_ATTENDENCE.ordinal, this)
        }

    }

    private fun getAttendanceModel(): RequestModel {
        return RequestModel(
            projectId = userInfo.projectId
        )
    }

    private fun getPerformanceModel(): RequestModel {
        return RequestModel(

        )
    }

    override fun onApiSuccess(o: String?, objectType: Int) {

        cancelProgressDialog()
        when (ApiExtentions.ApiDef.entries[objectType]) {

            ApiExtentions.ApiDef.GET_USER_DETAILS -> {
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val userResponse = Gson().fromJson(
                        model.getJSONObject("data").toString(),
                        UserDetails::class.java
                    )

                    mobiliserUsers.addAll(userResponse.users_mapped)
                    mobilisersAdapter = MobilisersAdapter(ArrayList(mobiliserUsers), this, requireContext())
                    binding.recyclerViewMobilisers.adapter = mobilisersAdapter

                    getPerformance()
                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }
            }

            ApiExtentions.ApiDef.GET_PERFORMANCE -> {
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val performanceData = Gson().fromJson(
                        model.getJSONObject("data").toString(),
                        PerformanceData::class.java
                    )
                    binding.txtVisits.text = performanceData.till_date.total_visits.toString()
                    binding.txtAttendance.text = performanceData.till_date.attendance.toString() + "%"
                    binding.txtTotalVisits.text = performanceData.till_date.audit_approval.toString() + "%"

                    getAttendance()
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

                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }

            }

            ApiExtentions.ApiDef.GET_LOGO -> {
                val model: ResponseModel = Gson().fromJson(o, ResponseModel::class.java)
                if (!model.error) {
                    val imageBytes = Base64.decode(model.data!!.get("logo").toString(), Base64.DEFAULT)
                    val decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                    binding.imgLogo.setImageBitmap(decodedImage)

                    // Get Mobilisers
                    getMobilisers()
                } else {
                    redirectionAlertDialogue(requireContext(), model.message!!)
                }
            }

            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }
    }

    override fun onApiError(message: String?) {
        if (message?.equals(context?.getString(R.string.session_expire))!!) {
            userInfo.authToken = ""
        }
        redirectionAlertDialogue(requireContext(), message!!)
    }

    override fun retry(type: Int) {

        when (ApiExtentions.ApiDef.values()[type]) {
            ApiExtentions.ApiDef.GET_USER_DETAILS -> getMobilisers()
            ApiExtentions.ApiDef.GET_ATTENDENCE -> getAttendance()
            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }

    }

    override fun redirectToAttendence(projectInfo: ProjectInfo) {

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
    }

    private fun redirectToCurriculam(projectInfo: ProjectInfo) {
        val intent = Intent(activity, Curriculam::class.java)
        intent.putExtra("projectInfo", Gson().toJson(projectInfo))
        startActivity(intent)
    }

    private fun showOptionsDialog() {
        val inflater = LayoutInflater.from(requireContext())
        val dialogView: View = inflater.inflate(R.layout.performance_dialog_options, null)

        val checkTillDate: RadioButton = dialogView.findViewById(R.id.check_till_date)
        val checkToday: RadioButton = dialogView.findViewById(R.id.check_today)
        val checkYesterday: RadioButton = dialogView.findViewById(R.id.check_yesterday)

        val builder = AlertDialog.Builder(requireContext())
        builder.setView(dialogView)
            .setTitle("Select Date Option")
            .setPositiveButton("OK") { dialog, _ ->
                if (checkTillDate.isChecked) {
                    checkToday.isChecked = false
                    checkYesterday.isChecked = false
                }
                if (checkToday.isChecked) {
                    checkYesterday.isChecked = false
                    checkTillDate.isChecked = false
                }
                if (checkYesterday.isChecked) {
                    checkToday.isChecked = false
                    checkTillDate.isChecked = false
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = builder.create()
        dialog.show()
    }

    override fun redirectToVisits(mobiliserData: MappedUser) {
        val bundle = Bundle()
        bundle.putString("mobiliserData", Gson().toJson(mobiliserData))
        findNavController().navigate(
            R.id.action_dashboard_to_visits,
            bundle
        )
    }
}