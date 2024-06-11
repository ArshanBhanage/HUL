package com.hul.curriculam.ui.schoolForm

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hul.HULApplication
import com.hul.R
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.controller.APIController
import com.hul.curriculam.CurriculamComponent
import com.hul.curriculam.ui.formDetails.FormDetailsFragment
import com.hul.curriculam.ui.formFill.FormFillFragment
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.data.SchoolCode
import com.hul.databinding.FragmentSchoolFormBinding
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import org.json.JSONObject
import java.lang.reflect.Type
import javax.inject.Inject

class SchoolFormFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentSchoolFormBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var curriculamComponent: CurriculamComponent

    @Inject
    lateinit var schoolFormViewModel: SchoolFormViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    private lateinit var adapter: PagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSchoolFormBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        curriculamComponent =
            (activity?.application as HULApplication).appComponent.curriculamComponent()
                .create()
        curriculamComponent.inject(this)
        schoolFormViewModel.selectedSchoolCode.value = Gson().fromJson(requireArguments().getString("schoolInformation"),SchoolCode::class.java)
        schoolFormViewModel.projectInfo.value = Gson().fromJson(requireArguments().getString("projectInfo"),ProjectInfo::class.java)
        binding.viewModel = schoolFormViewModel
        getVisitList()
        binding.stats.setOnClickListener {
            requireActivity().onBackPressed()
        }


        return root
    }

    private fun addNewTab(title: String, fragment: Fragment) {
        adapter.addFragment(fragment, title)
        adapter.notifyItemInserted(adapter.itemCount - 1)
    }

    fun getVisitList() {

        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            //setProgressDialog(requireContext(), "Loading Leads")
            apiController.getApiResponse(
                this,
                getVisitListModel(),
                ApiExtentions.ApiDef.VISIT_LIST_SINGLE.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.VISIT_LIST_SINGLE.ordinal, this)
        }

    }

    private fun getVisitListModel(): RequestModel {
        return RequestModel(
            location_id = schoolFormViewModel.projectInfo.value!!.location_id
        )
    }

    override fun onApiSuccess(o: String?, objectType: Int) {

        cancelProgressDialog()
        when (ApiExtentions.ApiDef.values()[objectType]) {

            ApiExtentions.ApiDef.VISIT_LIST_SINGLE -> {
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<ProjectInfo?>?>() {}.type
                    schoolFormViewModel.visitList.value =
                        Gson().fromJson(model.getJSONArray("data").toString(), listType);

                    adapter = PagerAdapter(requireActivity())
                    binding.viewPager.adapter = adapter

                    // Add fragments dynamically
                    for (visit in schoolFormViewModel.visitList.value!!)
                    {
                        if(!visit.visit_status.equals("completed", ignoreCase = true)) {
                            addNewTab(
                                requireContext().getString(R.string.visit) + visit.visit_number,
                                FormFillFragment.newInstance(Gson().toJson(schoolFormViewModel.selectedSchoolCode.value),Gson().toJson(schoolFormViewModel.projectInfo.value))
                            )
                        }
                        else{
                            addNewTab(
                                requireContext().getString(R.string.visit) + visit.visit_number,
                                FormDetailsFragment.newInstance(Gson().toJson(schoolFormViewModel.selectedSchoolCode.value),Gson().toJson(schoolFormViewModel.projectInfo.value))
                            )
                        }
                    }

                    if(schoolFormViewModel.visitList.value!!.size ==1)
                    {
                        binding.tabLayout.visibility = View.GONE
                    }
                    // Connect TabLayout with ViewPager2
                    TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                        tab.text = adapter.getPageTitle(position)
                    }.attach()

                    // Set the adapter to the AutoCompleteTextView
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
            ApiExtentions.ApiDef.VISIT_LIST_SINGLE -> getVisitList()
            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }

    }

}