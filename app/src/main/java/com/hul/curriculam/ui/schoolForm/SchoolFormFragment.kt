package com.hul.curriculam.ui.schoolForm

import android.os.Bundle
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
import com.hul.curriculam.ui.form1Fill.Form1FillFragment
import com.hul.curriculam.ui.form2Details.Form2DetailsFragment
import com.hul.curriculam.ui.form2Fill.Form2FillFragment
import com.hul.curriculam.ui.form3Details.Form3DetailsFragment
import com.hul.curriculam.ui.form3Fill.Form3FillFragment
import com.hul.data.ProjectInfo
import com.hul.data.SchoolCode
import com.hul.databinding.FragmentSchoolFormBinding
import com.hul.user.UserInfo
import com.hul.utils.ASSIGNED
import com.hul.utils.INITIATED
import com.hul.utils.PARTIALLY_SUBMITTED
import com.hul.utils.RetryInterface
import com.hul.utils.SUBMITTED
import com.hul.utils.cancelProgressDialog
import com.hul.utils.redirectionAlertDialogue
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
        schoolFormViewModel.selectedSchoolCode.value = Gson().fromJson(
            requireArguments().getString("schoolInformation"),
            SchoolCode::class.java
        )

        binding.viewModel = schoolFormViewModel
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

        schoolFormViewModel.visitList.value =
            Gson().fromJson(requireArguments().getString("visitList"), listType);

        val uDiceCode = requireArguments().getString("uDiceCode")

        adapter = PagerAdapter(requireActivity())
        binding.viewPager.adapter = adapter

        var currentVisit: ProjectInfo? = null;
        var completedVisit: ProjectInfo? = null;

        var firstVisit: ProjectInfo? = null

        for (visit in schoolFormViewModel.visitList.value!!) {
            if (visit.visit_number == "1") {
                firstVisit = visit
            }
        }

        // Add fragments dynamically
        for (visit in schoolFormViewModel.visitList.value!!) {
            if ((visit.visit_status.equals(ASSIGNED, ignoreCase = true)
                || visit.visit_status.equals(INITIATED, ignoreCase = true)
                || visit.visit_status.equals(PARTIALLY_SUBMITTED, ignoreCase = true))
            ) {
                currentVisit = visit
                visit.number_of_books_distributed = firstVisit?.number_of_books_distributed
                when (visit.visit_number) {
                    "1" -> addNewTab(
                        requireContext().getString(R.string.visit) + visit.visit_number,
                        Form1FillFragment.newInstance(
                            Gson().toJson(schoolFormViewModel.selectedSchoolCode.value),
                            Gson().toJson(visit),
                            uDiceCode
                        )
                    )

                    "2" -> addNewTab(
                        requireContext().getString(R.string.visit) + visit.visit_number,
                        Form2FillFragment.newInstance(
                            Gson().toJson(schoolFormViewModel.selectedSchoolCode.value),
                            Gson().toJson(visit),
                            uDiceCode
                        )
                    )

                    "3" -> addNewTab(
                        requireContext().getString(R.string.visit) + visit.visit_number,
                        Form3FillFragment.newInstance(
                            Gson().toJson(schoolFormViewModel.selectedSchoolCode.value),
                            Gson().toJson(visit),
                            uDiceCode
                        )
                    )

                }

            } else {
                completedVisit = visit
                visit.number_of_books_distributed = firstVisit?.number_of_books_distributed
                when (visit.visit_number) {
                    "1" -> addNewTab(
                        requireContext().getString(R.string.visit) + visit.visit_number,
                        Form1DetailsFragment.newInstance(
                            Gson().toJson(schoolFormViewModel.selectedSchoolCode.value),
                            Gson().toJson(visit),
                            uDiceCode,
                            requireArguments().getString("localData")

                        )
                    )

                    "2" -> addNewTab(
                        requireContext().getString(R.string.visit) + visit.visit_number,
                        Form2DetailsFragment.newInstance(
                            Gson().toJson(schoolFormViewModel.selectedSchoolCode.value),
                            Gson().toJson(visit),
                            uDiceCode,
                            requireArguments().getString("localData")
                        )
                    )

                    "3" -> addNewTab(
                        requireContext().getString(R.string.visit) + visit.visit_number,
                        Form3DetailsFragment.newInstance(
                            Gson().toJson(schoolFormViewModel.selectedSchoolCode.value),
                            Gson().toJson(visit),
                            uDiceCode,
                            requireArguments().getString("localData")
                        )
                    )

                }

            }
        }

        if (schoolFormViewModel.visitList.value!!.size == 1) {
            binding.tabLayout.visibility = View.GONE
        }
        // Connect TabLayout with ViewPager2
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = adapter.getPageTitle(position)
        }.attach()

        binding.visitTitle.text =
            "Visit " + schoolFormViewModel.visitList.value!![0].visit_number + " School Activity"
        binding.visitSubTitle.text =
            if (schoolFormViewModel.visitList.value!![0].visit_status.equals(
                    ASSIGNED,
                    ignoreCase = true
                )
                || schoolFormViewModel.visitList.value!![0].visit_status.equals(
                    INITIATED,
                    ignoreCase = true
                )
                || schoolFormViewModel.visitList.value!![0].visit_status.equals(
                    PARTIALLY_SUBMITTED,
                    ignoreCase = true
                )
            )
                "Fill up the following details" else "Find visit details below"

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.visitTitle.text =
                    "Visit " + schoolFormViewModel.visitList.value!![tab.position].visit_number + " School Activity"

                binding.visitSubTitle.text =
                    if (schoolFormViewModel.visitList.value!![tab.position].visit_status.equals(
                            ASSIGNED,
                            ignoreCase = true
                        )
                        || schoolFormViewModel.visitList.value!![tab.position].visit_status.equals(
                            INITIATED,
                            ignoreCase = true
                        )|| schoolFormViewModel.visitList.value!![tab.position].visit_status.equals(
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

        // ToDo : Below code is only changing tabs not the fragment, viewpager impl needs to be changed
        /*currentVisit = schoolFormViewModel.visitList.value?.firstOrNull {
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