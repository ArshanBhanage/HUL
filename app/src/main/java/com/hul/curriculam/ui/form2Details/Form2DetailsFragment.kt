package com.hul.curriculam.ui.form2Details

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
import com.hul.data.GetVisitDataResponseData
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.data.SchoolCode
import com.hul.databinding.FragmentForm2Binding
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

class Form2DetailsFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentForm2Binding? = null

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

        _binding = FragmentForm2Binding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        curriculamComponent =
            (activity?.application as HULApplication).appComponent.curriculamComponent()
                .create()
        curriculamComponent.inject(this)

        form2ViewModel.selectedSchoolCode.value = Gson().fromJson(
            requireArguments().getString(ARG_CONTENT1),
            SchoolCode::class.java
        )

        form2ViewModel.projectInfo.value = Gson().fromJson(
            requireArguments().getString(ARG_CONTENT2),
            ProjectInfo::class.java
        )

        form2ViewModel.uDiceCode.value = requireArguments().getString(U_DICE_CODE)

        binding.viewModel = form2ViewModel
        return root
    }

    companion object {
        private const val ARG_CONTENT1 = "content1"
        private const val ARG_CONTENT2 = "content2"
        private const val U_DICE_CODE = "uDiceCode"

        fun newInstance(content1: String, content2: String, uDiceCode: String?) =
            Form2DetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONTENT1, content1)
                    putString(ARG_CONTENT2, content2)
                    putString(U_DICE_CODE, uDiceCode)
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
                loadImages = true
            )
        }!!
    }

    private fun getVisitData() {
        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            //setProgressDialog(requireContext(), "Loading Visit data")
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
        when (ApiExtentions.ApiDef.entries[objectType]) {

            ApiExtentions.ApiDef.GET_VISIT_DATA -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                form2ViewModel.visitData.value = Gson().fromJson(
                    model.getJSONObject("data").toString(),
                    GetVisitDataResponseData::class.java
                )

                fillData()

                if (form2ViewModel.visitData.value?.visit_2?.visit_image_1?.value != null) {
                    loadImage(
                        form2ViewModel.visitData.value?.visit_2?.visit_image_1!!.value.toString(),
                        binding.img1, binding.llImg1
                    )
                }

                if (form2ViewModel.visitData.value?.visit_2?.visit_image_2?.value != null) {
                    loadImage(
                        form2ViewModel.visitData.value?.visit_2?.visit_image_2!!.value.toString(),
                        binding.img2, binding.llImg2
                    )
                }

                if (form2ViewModel.visitData.value?.visit_2?.visit_image_3?.value != null) {
                    loadImage(
                        form2ViewModel.visitData.value?.visit_2?.visit_image_3!!.value.toString(),
                        binding.img3, binding.llImg3
                    )
                }

                if (form2ViewModel.visitData.value?.visit_2?.visit_image_4?.value != null) {
                    loadImage(
                        form2ViewModel.visitData.value?.visit_2?.visit_image_4!!.value.toString(),
                        binding.img4, binding.llImg4
                    )
                }

                // For render purpose only
                /*if (form2ViewModel.visitData.value?.visit_1 != null) {
                    form2ViewModel.visitDataToView.value = form2ViewModel.visitData.value?.visit_1
                } else if (form2ViewModel.visitData.value?.visit_2 != null) {
                    form2ViewModel.visitDataToView.value = form2ViewModel.visitData.value?.visit_2
                } else if (form2ViewModel.visitData.value?.visit_3 != null) {
                    form2ViewModel.visitDataToView.value = form2ViewModel.visitData.value?.visit_3
                }*/
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

    private fun fillData() {
        binding.txtUdiceCode.text = form2ViewModel.uDiceCode.value
        binding.txtSchoolName.text = form2ViewModel.selectedSchoolCode.value?.location_name
        binding.txtNoOfBooksGiven.text =
            form2ViewModel.projectInfo.value?.number_of_books_distributed.toString()
        binding.txtCurriculamOnTrack.text =
            if (form2ViewModel.visitData.value?.visit_2?.mobile_number_of_the_school_representative_who_collected_the_books?.value == 1) "Yes" else "No"
        binding.txtNameOfSchoolRepresentative.text =
            form2ViewModel.visitData.value?.visit_2?.name_of_the_school_representative_who_collected_the_books?.value?.toString() ?: ""

        binding.txtNumberOfSchoolRepresentative.text =
            form2ViewModel.visitData.value?.visit_2?.mobile_number_of_the_school_representative_who_collected_the_books?.value?.toString() ?: ""

        binding.txtRemark.text =
            form2ViewModel.visitData.value?.visit_2?.remark?.value?.toString() ?: ""

        binding.txtNoOfBooksGiven.text =
            form2ViewModel.projectInfo.value?.number_of_books_distributed ?: ""

    }

    private fun loadImage(base64: String, imgId: ImageView, llId: LinearLayout) {

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
        binding.img4.setImageBitmap(null)
    }
}