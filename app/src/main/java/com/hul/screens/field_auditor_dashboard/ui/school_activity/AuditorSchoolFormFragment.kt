package com.hul.screens.field_auditor_dashboard.ui.school_activity

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
import com.hul.curriculam.ui.form3Fill.Form3FillFragment
import com.hul.curriculam.ui.schoolForm.PagerAdapter
import com.hul.curriculam.ui.schoolForm.SchoolFormViewModel
import com.hul.data.ProjectInfo
import com.hul.data.SchoolCode
import com.hul.databinding.FragmentSchoolFormAuditorBinding
import com.hul.databinding.FragmentSchoolFormBinding
import com.hul.screens.field_auditor_dashboard.ui.school_activity.form1Details.AuditorForm1DetailsFragment
import com.hul.screens.field_auditor_dashboard.ui.school_activity.form1Fill.AuditorForm1FillFragment
import com.hul.screens.field_auditor_dashboard.ui.school_activity.form2Details.AuditorForm2DetailsFragment
import com.hul.screens.field_auditor_dashboard.ui.school_activity.form2Fill.AuditorForm2FillFragment
import com.hul.screens.field_auditor_dashboard.ui.school_activity.form3Fill.AuditorForm3FillFragment
import com.hul.user.UserInfo
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.redirectionAlertDialogue
import java.lang.reflect.Type
import javax.inject.Inject

class AuditorSchoolFormFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentSchoolFormAuditorBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var curriculamComponent: CurriculamComponent

    @Inject
    lateinit var schoolFormViewModel: AuditorSchoolFormViewModel

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

        _binding = FragmentSchoolFormAuditorBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        curriculamComponent =
            (activity?.application as HULApplication).appComponent.curriculamComponent()
                .create()
        curriculamComponent.inject(this)

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

        schoolFormViewModel.projectInfo.value =
            Gson().fromJson(requireArguments().getString("projectInfo"), ProjectInfo::class.java);

        adapter = PagerAdapter(requireActivity())
        binding.viewPager.adapter = adapter

        var currentVisit: ProjectInfo? = null;
        var completedVisit: ProjectInfo? = null;

        // Add fragments dynamically
        for (visit in schoolFormViewModel.visitList.value!!) {
            if (visit.visit_status.equals("SUBMITTED", ignoreCase = true)
            ) {
                currentVisit = visit
                when (visit.visit_number) {
                    "1" -> addNewTab(
                        requireContext().getString(R.string.visit) + visit.visit_number,
                        AuditorForm1FillFragment.newInstance(
                            Gson().toJson(schoolFormViewModel.visitList.value),
                            Gson().toJson(schoolFormViewModel.projectInfo.value)
                        )
                    )

                    "2" -> addNewTab(
                        requireContext().getString(R.string.visit) + visit.visit_number,
                        AuditorForm2FillFragment.newInstance(
                            Gson().toJson(schoolFormViewModel.visitList.value),
                            Gson().toJson(schoolFormViewModel.projectInfo.value)
                        )
                    )

                    "3" -> addNewTab(
                        requireContext().getString(R.string.visit) + visit.visit_number,
                        AuditorForm3FillFragment.newInstance(
                            Gson().toJson(schoolFormViewModel.visitList.value),
                            Gson().toJson(schoolFormViewModel.projectInfo.value)
                        )
                    )

                }

            } else {
                completedVisit = visit
                when (visit.visit_number) {
                    "1" -> addNewTab(
                        requireContext().getString(R.string.visit) + visit.visit_number,
                        AuditorForm1DetailsFragment.newInstance(
                            Gson().toJson(schoolFormViewModel.visitList.value),
                            Gson().toJson(schoolFormViewModel.projectInfo.value)
                        )
                    )

                    "2" -> addNewTab(
                        requireContext().getString(R.string.visit) + visit.visit_number,
                        AuditorForm2DetailsFragment.newInstance(
                            Gson().toJson(schoolFormViewModel.visitList.value),
                            Gson().toJson(schoolFormViewModel.projectInfo.value)
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
                    "ASSIGNED",
                    ignoreCase = true
                )
                || schoolFormViewModel.visitList.value!![0].visit_status.equals(
                    "INITIATED",
                    ignoreCase = true
                )
            )
                "Select your visit for this school" else "Fill up the following details"

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                binding.visitTitle.text =
                    "Visit " + schoolFormViewModel.visitList.value!![tab.position].visit_number + " School Activity"
                binding.visitSubTitle.text =
                    if (schoolFormViewModel.visitList.value!![tab.position].visit_status.equals(
                            "ASSIGNED",
                            ignoreCase = true
                        )
                        || schoolFormViewModel.visitList.value!![tab.position].visit_status.equals(
                            "INITIATED",
                            ignoreCase = true
                        )
                    )
                        "Select your visit for this school" else "Fill up the following details"
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
                // Code to handle tab unselection
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
                // Code to handle tab reselection
            }
        })

        // Set the adapter to the AutoCompleteTextView

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