package com.hul.web_form.dynamic_forms

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hul.HULApplication
import com.hul.R
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.controller.APIController
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.databinding.FragmentDynamicFormBinding
import com.hul.user.UserInfo
import com.hul.utils.ASSIGNED
import com.hul.utils.ConnectionDetector
import com.hul.utils.INITIATED
import com.hul.utils.PARTIALLY_SUBMITTED
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import com.hul.web_form.WebFormComponent
import com.hul.web_form.dynamicFormDetails.DynamicFormDetailsFragment
import com.hul.web_form.dynamicFormFill.DynamicFormFillFragment
import org.json.JSONObject
import java.lang.reflect.Type
import javax.inject.Inject

class DynamicFormFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentDynamicFormBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var webFormComponent: WebFormComponent

    @Inject
    lateinit var dynamicFormViewModel: DynamicFormViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    private lateinit var dynamicPagerAdapter: DynamicPagerAdapter

    var visitList: ArrayList<ProjectInfo> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDynamicFormBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        webFormComponent =
            (activity?.application as HULApplication).appComponent.webFormComponent()
                .create()
        webFormComponent.inject(this)
//        dynamicFormViewModel.selectedSchoolCode.value = Gson().fromJson(
//            requireArguments().getString("schoolInformation"),
//            SchoolCode::class.java
//        )
        if (requireArguments() != null) {
            dynamicFormViewModel.projectInfo.value = Gson().fromJson(
                requireArguments().getString("projectInfo"),
                ProjectInfo::class.java
            )

            binding.viewModel = dynamicFormViewModel

            binding.stats.setOnClickListener {
                requireActivity().finish()
            }

            getVisits(dynamicFormViewModel.projectInfo.value!!.location_id!!)
        }
        return root
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listType: Type = object : TypeToken<List<ProjectInfo?>?>() {}.type

//        dynamicFormViewModel.visitList.value =
//            Gson().fromJson(requireArguments().getString("visitList"), listType);
//
//        val uDiceCode = requireArguments().getString("uDiceCode")

        dynamicPagerAdapter = DynamicPagerAdapter(requireActivity())
        binding.viewPager.adapter = dynamicPagerAdapter

        var currentVisit: ProjectInfo? = null;
        var completedVisit: ProjectInfo? = null;

        var firstVisit: ProjectInfo? = null

        // Connect TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = dynamicPagerAdapter.getPageTitle(position)
        }.attach()


    }

    private fun addNewTab(title: String, fragment: Fragment) {
        dynamicPagerAdapter.addFragment(fragment, title)
        dynamicPagerAdapter.notifyItemInserted(dynamicPagerAdapter.itemCount - 1)
    }

    private fun setTabs()
    {
        if (visitList.size == 1) {
            binding.tabLayout.visibility = View.GONE
        }
        binding.visitTitle.text =
            "Visit " +visitList[0].visit_number + " House Activity"
        binding.visitSubTitle.text =
            if (visitList[0].visit_status.equals(
                    ASSIGNED,
                    ignoreCase = true
                )
                || visitList[0].visit_status.equals(
                    INITIATED,
                    ignoreCase = true
                )
                || visitList[0].visit_status.equals(
                    PARTIALLY_SUBMITTED,
                    ignoreCase = true
                )
            )
                "Fill up the following details" else "Find visit details below"
        for (visit in visitList) {
            if ((visit.visit_status.equals(ASSIGNED, ignoreCase = true)
                        || visit.visit_status.equals(INITIATED, ignoreCase = true)
                        || visit.visit_status.equals(PARTIALLY_SUBMITTED, ignoreCase = true))
            ) {
                addNewTab(
                        requireContext().getString(R.string.visit) + visit.visit_number,
                        DynamicFormFillFragment.newInstance(
                            Gson().toJson(visit)
                        )
                    )


            } else {
                addNewTab(
                    requireContext().getString(R.string.visit) + visit.visit_number,
                    DynamicFormDetailsFragment.newInstance(
                        Gson().toJson(visit)
                    )
                )

            }
        }

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {

                binding.visitTitle.text =
                    "Visit " + visitList[tab.position].visit_number + " School Activity"

                binding.visitSubTitle.text =
                    if (visitList[tab.position].visit_status.equals(
                            ASSIGNED,
                            ignoreCase = true
                        )
                        || visitList[tab.position].visit_status.equals(
                            INITIATED,
                            ignoreCase = true
                        )|| visitList[tab.position].visit_status.equals(
                            PARTIALLY_SUBMITTED,
                            ignoreCase = true
                        )
                    )
                        "Fill up the following details" else "Find visit details below"
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Code to handle tab un-selection
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Code to handle tab reselection
            }
        })
    }

    private fun getVisits(id: String) {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            //setProgressDialog(requireContext(), "Loading Leads")
            apiController.getApiResponse(
                this,
                getVisitsModel(id),
                ApiExtentions.ApiDef.VISIT_LIST_BY_ID.ordinal
            )
        } else {
            noInternetDialogue(
                requireContext(),
                ApiExtentions.ApiDef.VISIT_LIST_BY_ID.ordinal,
                this
            )
        }

    }

    private fun getVisitsModel(id: String): RequestModel {
        return RequestModel(
            visit_id = id,
        )
    }

    override fun onApiSuccess(o: String?, objectType: Int) {


        when (ApiExtentions.ApiDef.entries[objectType]) {

            ApiExtentions.ApiDef.VISIT_LIST_BY_ID -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<ProjectInfo?>?>() {}.type
                    visitList =
                        Gson().fromJson(model.getJSONArray("data").toString(), listType);
                    setTabs()
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

}