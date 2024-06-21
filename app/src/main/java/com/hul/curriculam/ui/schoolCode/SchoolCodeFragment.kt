package com.hul.curriculam.ui.schoolCode

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.hul.HULApplication
import com.hul.R
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.controller.APIController
import com.hul.curriculam.CurriculamComponent
import com.hul.data.RequestModel
import com.hul.data.SchoolCode
import com.hul.databinding.FragmentSchoolCodeBinding
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import org.json.JSONObject
import java.lang.reflect.Type
import java.util.Collections
import javax.inject.Inject

class SchoolCodeFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentSchoolCodeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var curriculamComponent: CurriculamComponent

    @Inject
    lateinit var schoolCodeViewModel: SchoolCodeViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    var adapter: SchoolCodeAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSchoolCodeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        curriculamComponent =
            (activity?.application as HULApplication).appComponent.curriculamComponent()
                .create()
        curriculamComponent.inject(this)
        binding.viewModel = schoolCodeViewModel
        binding.schoolCode.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Code to execute before the text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Code to execute when the text is changed
                getSchoolCodes(binding.schoolCode.text.toString())
            }

            override fun afterTextChanged(s: Editable?) {
                // Code to execute after the text is changed
            }
        })
        binding.stats.setOnClickListener {
            requireActivity().onBackPressed()
        }
        binding.searchButton.setOnClickListener {
            getSchoolCodes(binding.schoolCode.text.toString())
        }

        binding.proceed.setOnClickListener {
            redirectToSchoolForm()
        }

        binding.schoolCode.setOnItemClickListener { parent, view, position, id ->
            schoolCodeViewModel.selectedSchoolCode.value =
                parent.getItemAtPosition(position) as SchoolCode
            val selectedName =
                schoolCodeViewModel.selectedSchoolCode.value!!.external_id1  // Get the specific key
            binding.schoolCode.setText(selectedName)  // Set the specific key to the AutoCompleteTextView
            binding.schoolName.setText(schoolCodeViewModel.selectedSchoolCode.value!!.location_name)
            binding.ward.setText(schoolCodeViewModel.selectedSchoolCode.value!!.location_address)
            binding.district.setText(schoolCodeViewModel.selectedSchoolCode.value!!.location_address)
        }
        return root
    }

    override fun onResume() {
        super.onResume()
        adapter?.updateVisits(Collections.emptyList())
        binding.schoolName.setText("")
        binding.ward.setText("")
        binding.district.setText("")
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

    override fun onApiSuccess(o: String?, objectType: Int) {

        cancelProgressDialog()
        when (ApiExtentions.ApiDef.entries[objectType]) {

            ApiExtentions.ApiDef.SCHOOL_CODES -> {
                val model = JSONObject(o.toString())
                if (!model.getBoolean("error")) {
                    val listType: Type = object : TypeToken<List<SchoolCode?>?>() {}.type
                    val schoolCodes: ArrayList<SchoolCode> =
                        Gson().fromJson(model.getJSONArray("data").toString(), listType);
                    adapter = SchoolCodeAdapter(
                        requireContext(),
                        R.layout.school_code_dropdown,
                        schoolCodes
                    )

                    // Set the adapter to the AutoCompleteTextView
                    binding.schoolCode.setAdapter(adapter)
                    binding.schoolCode.requestFocus()
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
            ApiExtentions.ApiDef.SCHOOL_CODES -> getSchoolCodes(binding.schoolCode.text.toString())
            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }

    }

    private fun redirectToSchoolForm() {
        if(schoolCodeViewModel.selectedSchoolCode.value == null
            || requireArguments().getString("projectInfo") == null) {
            Toast.makeText(requireContext(), "Please fill all the details", Toast.LENGTH_LONG).show()
            return;
        }
        val bundle = Bundle()
        bundle.putString(
            "schoolInformation",
            Gson().toJson(schoolCodeViewModel.selectedSchoolCode.value)
        )
        bundle.putString(
            "projectInfo",
            requireArguments().getString("projectInfo")
        )
        findNavController().navigate(
            R.id.action_schoolCodeFragment_to_schoolFormFragment,
            bundle
        )
    }

}