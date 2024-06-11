package com.hul.screens.field_auditor_dashboard.ui.mobiliser_visits

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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
import com.hul.curriculam.Curriculam
import com.hul.dashboard.ui.dashboard.AttendenceAdapter
import com.hul.dashboard.ui.dashboard.MyVisitsAdapter
import com.hul.data.Attendencemodel
import com.hul.data.MappedUser
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.databinding.FragmentSchoolActivityBinding
import com.hul.databinding.FragmentVisitsBinding
import com.hul.screens.field_auditor_dashboard.FieldAuditorDashboardComponent
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import com.hul.utils.setProgressDialog
import org.json.JSONObject
import java.lang.reflect.Type
import javax.inject.Inject


class MobiliserVisitsFragment : Fragment(), ApiHandler, RetryInterface,
    MobiliserVisitsFragmentInterface {

    private var _binding: FragmentVisitsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var dashboardComponent: FieldAuditorDashboardComponent

    @Inject
    lateinit var uploadFileController: UploadFileController

    @Inject
    lateinit var mobiliserVisitsViewModel: MobiliserVisitsViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentVisitsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        dashboardComponent =
            (activity?.application as HULApplication).appComponent.fieldAuditorDashboardComponent()
                .create()
        dashboardComponent.inject(this)
        binding.viewModel = mobiliserVisitsViewModel

        binding.recyclerViewVisits.layoutManager = LinearLayoutManager(context)

        binding.stats.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        getVisits()
    }

    private fun getVisitListModel(userType: String, mobiliserId: Int): RequestModel {
        return RequestModel(
            userType = userType,
            mobiliserId = mobiliserId
        )
    }

    private fun getVisits() {

        mobiliserVisitsViewModel.mobiliserUser.value =
            Gson().fromJson(requireArguments().getString("mobiliserData"), MappedUser::class.java)

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {

            mobiliserVisitsViewModel.mobiliserUser.value?.let {
                apiController.getApiResponse(
                    this,
                    mobiliserVisitsViewModel.mobiliserUser.value?.let {
                        getVisitListModel(
                            userInfo.userType,
                            it.user_id
                        )
                    },
                    ApiExtentions.ApiDef.VISIT_LIST_FIELD_AUDITOR.ordinal
                )
            }
        } else {
            noInternetDialogue(
                requireContext(),
                ApiExtentions.ApiDef.VISIT_LIST_FIELD_AUDITOR.ordinal,
                this
            )
        }

    }

    override fun onApiSuccess(o: String?, objectType: Int) {
        cancelProgressDialog()
        when (ApiExtentions.ApiDef.entries[objectType]) {

            ApiExtentions.ApiDef.VISIT_LIST_FIELD_AUDITOR -> {
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<ProjectInfo?>?>() {}.type
                    val visits: ArrayList<ProjectInfo> =
                        Gson().fromJson(model.getJSONArray("data").toString(), listType);

                    val myVisitsAdapter =
                        MobiliserVisitsAdapter(visits, this, requireContext())
                    binding.recyclerViewVisits.adapter = myVisitsAdapter
                } else {
                    redirectionAlertDialogue(requireContext(), model.getString("message"))
                }

            }

            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }
    }

    override fun onApiError(message: String?) {

    }

    override fun retry(type: Int) {

    }

    override fun redirectToSchoolActivity(projectInfo: ProjectInfo) {
        val bundle = Bundle()
        bundle.putString("projectInfo", Gson().toJson(projectInfo))
        findNavController().navigate(
            R.id.action_visits_school_activity,
            bundle
        )
    }


}