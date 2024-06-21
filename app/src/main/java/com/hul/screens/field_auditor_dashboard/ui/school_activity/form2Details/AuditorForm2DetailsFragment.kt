package com.hul.screens.field_auditor_dashboard.ui.school_activity.form2Details

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.hul.HULApplication
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.controller.APIController
import com.hul.curriculam.CurriculamComponent
import com.hul.curriculam.ui.form2Details.Form2ViewModel
import com.hul.data.GetVisitDataResponseData
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.databinding.AuditorFragmentForm2Binding
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import com.hul.utils.setProgressDialog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

class AuditorForm2DetailsFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: AuditorFragmentForm2Binding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var curriculamComponent: CurriculamComponent

    @Inject
    lateinit var form2ViewModel: Form2ViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = AuditorFragmentForm2Binding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        curriculamComponent =
            (activity?.application as HULApplication).appComponent.curriculamComponent()
                .create()
        curriculamComponent.inject(this)

        form2ViewModel.projectInfo.value = Gson().fromJson(
            requireArguments().getString(PROJECT_INFO),
            ProjectInfo::class.java
        )

        binding.viewModel = form2ViewModel
        return root
    }

    companion object {
        private const val VISIT_LIST = "visitList"
        private const val PROJECT_INFO = "projectInfo"

        fun newInstance(visitList: String, projectInfo: String) = AuditorForm2DetailsFragment().apply {
            arguments = Bundle().apply {
                putString(VISIT_LIST, visitList)
                putString(PROJECT_INFO, projectInfo)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getVisitData()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun visitsDataModel(): RequestModel {
        return form2ViewModel.projectInfo.value?.visit_id?.let {
            RequestModel(
                project = userInfo.projectName,
                visitId = it,
                loadImages = true,
                collected_by = "FIELD_AUDITOR"
            )
        }!!
    }

    private fun getVisitData() {
        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            setProgressDialog(requireContext(), "Loading Visit data")
            apiController.getApiResponse(
                this,
                visitsDataModel(),
                ApiExtentions.ApiDef.GET_VISIT_DATA.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.GET_VISIT_DATA.ordinal, this)
        }
    }

    override fun onApiSuccess(o: String?, objectType: Int) {
        cancelProgressDialog()
        when (ApiExtentions.ApiDef.entries[objectType]) {

            ApiExtentions.ApiDef.GET_VISIT_DATA -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                form2ViewModel.visitData.value = Gson().fromJson(
                    model.getJSONObject("data").toString(),
                    GetVisitDataResponseData::class.java
                )

                if (form2ViewModel.visitData.value?.visit_2?.auditor_visit_image_1?.value != null) {
                    loadImage(
                        form2ViewModel.visitData.value?.visit_2?.auditor_visit_image_1!!.value.toString(),
                        binding.img1, binding.llImg1)
                }

                if (form2ViewModel.visitData.value?.visit_2?.auditor_visit_image_2?.value != null) {
                    loadImage(
                        form2ViewModel.visitData.value?.visit_2?.auditor_visit_image_2!!.value.toString(),
                        binding.img2, binding.llImg2)
                }

                if (form2ViewModel.visitData.value?.visit_2?.auditor_visit_image_3?.value != null) {
                    loadImage(
                        form2ViewModel.visitData.value?.visit_2?.auditor_visit_image_3!!.value.toString(),
                        binding.img3, binding.llImg3)
                }

            }

            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }
    }

    override fun onApiError(message: String?) {
        cancelProgressDialog()
        redirectionAlertDialogue(requireContext(), message!!)
    }

    override fun retry(type: Int) {
        when (ApiExtentions.ApiDef.entries[type]) {
            else -> Toast.makeText(requireContext(), "Api Not Integrated", Toast.LENGTH_LONG).show()
        }
    }

    private fun loadImage(base64: String, imgId: ImageView, llId: LinearLayout) {

        binding.llImages.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val decodedByte = withContext(Dispatchers.IO) {
                    val decodedString = Base64.decode(base64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                }

                Glide.with(imgId.context)
                    .load(decodedByte)
                    .into(imgId)

                llId.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.img1.setImageBitmap(null)
        binding.img2.setImageBitmap(null)
        binding.img3.setImageBitmap(null)
    }
}