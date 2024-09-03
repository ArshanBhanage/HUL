package com.hul.salg.ui.salgForm

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
import com.hul.curriculam.CurriculamComponent
import com.hul.curriculam.ui.form1Details.Form1DetailsFragment
import com.hul.curriculam.ui.form2Details.Form2DetailsFragment
import com.hul.curriculam.ui.form2Fill.Form2FillFragment
import com.hul.curriculam.ui.form3Details.Form3DetailsFragment
import com.hul.curriculam.ui.form3Fill.Form3FillFragment
import com.hul.curriculam.ui.schoolForm.PagerAdapter
import com.hul.data.ProjectInfo
import com.hul.data.SchoolCode
import com.hul.data.Society
import com.hul.databinding.FragmentSalgFormBinding
import com.hul.databinding.FragmentSchoolFormBinding
import com.hul.salg.SalgDashboardComponent
import com.hul.salg.ui.formFill.SalgFormFillFragment
import com.hul.user.UserInfo
import com.hul.utils.ASSIGNED
import com.hul.utils.INITIATED
import com.hul.utils.PARTIALLY_SUBMITTED
import com.hul.utils.RetryInterface
import com.hul.utils.SUBMITTED
import com.hul.utils.cancelProgressDialog
import com.hul.utils.redirectionAlertDialogue
import java.lang.reflect.Type
import java.util.List
import javax.inject.Inject

class SalgFormFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentSalgFormBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var salgDashboardComponent: SalgDashboardComponent

    @Inject
    lateinit var salgFormViewModel: SalgFormViewModel

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

        _binding = FragmentSalgFormBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        salgDashboardComponent =
            (activity?.application as HULApplication).appComponent.salgDashboardComponent().create()
        salgDashboardComponent.inject(this)

        salgFormViewModel.projectInfo.value =
            Gson().fromJson(requireArguments().getString("projectInfo"), Society::class.java)

        binding.viewModel = salgFormViewModel

        binding.visitTitle.text = "Flat"+requireArguments().getString("flatNumber")!!+"Visit"

        binding.visitSubTitle.text = salgFormViewModel.projectInfo.value!!.location_name

        binding.stats.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return root
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val listType: Type = object : TypeToken<List<ProjectInfo?>?>() {}.type

//        salgFormViewModel.visitList.value =
//            Gson().fromJson(requireArguments().getString("visitList"), listType);

        val uDiceCode = requireArguments().getString("uDiceCode")

        adapter = PagerAdapter(requireActivity())
        binding.viewPager.adapter = adapter

        var currentVisit: ProjectInfo? = null;
        var completedVisit: ProjectInfo? = null;

        var firstVisit: ProjectInfo? = null

//        for (visit in salgFormViewModel.visitList.value!!) {
//            if (visit.visit_number == "1") {
//                firstVisit = visit
//            }
//        }
        addNewTab(
            requireContext().getString(R.string.visit) + "1",
            SalgFormFillFragment.newInstance(
                requireArguments().getString("projectInfo")!!,
                requireArguments().getString("wingNumber")!!,
                requireArguments().getString("floor")!!,
                requireArguments().getString("flatNumber")!!,
                requireArguments().getString("imageUrl1")!!,
                requireArguments().getString("response")!!,

            )
        )

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()
        // Add fragments dynamically
        // Connect TabLayout with ViewPager2

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {

            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Code to handle tab un-selection
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Code to handle tab reselection
            }
        })

        // ToDo : Below code is only changing tabs not the fragment, viewpager impl needs to be changed
        /*currentVisit = salgFormViewModel.visitList.value?.firstOrNull {
            it.visit_status.equals("ASSIGNED", ignoreCase = true)
                    || it.visit_status.equals("INITIATED", ignoreCase = true)
        }
        currentVisit?.visit_number?.toIntOrNull()?.let {
            binding.viewPager.currentItem = it - 1
        }*/

    }

    private fun addNewTab(title: String, fragment: Fragment) {
        adapter.addFragment(fragment, title)
        adapter.notifyItemInserted(adapter.itemCount - 1)
    }

    override fun onApiSuccess(o: String?, objectType: Int) {

        cancelProgressDialog()
        when (ApiExtentions.ApiDef.entries[objectType]) {

            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }
    }

    override fun onApiError(message: String?) {
        redirectionAlertDialogue(requireContext(), message!!)
    }

    override fun retry(type: Int) {

        when (ApiExtentions.ApiDef.entries[type]) {
            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }

    }

}