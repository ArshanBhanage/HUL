package com.hul.salg.ui.formDetails

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.hul.HULApplication
import com.hul.R
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.controller.APIController
import com.hul.curriculam.CurriculamComponent
import com.hul.curriculam.ui.form1Details.Form1DetailsFragment
import com.hul.curriculam.ui.form1Details.Form1ViewModel
import com.hul.data.GetVisitDataResponseData
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.data.SchoolCode
import com.hul.databinding.FragmentForm1Binding
import com.hul.databinding.FragmentSalgFormDetailBinding
import com.hul.salg.SalgDashboardComponent
import com.hul.salg.ui.formFill.SalgFormFillViewModel
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

class SalgFormDetailFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentSalgFormDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var salgDashboardComponent: SalgDashboardComponent

    @Inject
    lateinit var salgFormDetailViewModel: SalgFormDetailViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSalgFormDetailBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        salgDashboardComponent =
            (activity?.application as HULApplication).appComponent.salgDashboardComponent()
                .create()
        salgDashboardComponent.inject(this)

        salgFormDetailViewModel.projectInfo.value = Gson().fromJson(
            requireArguments().getString(ARG_CONTENT2),
            ProjectInfo::class.java
        )


//        salgFormDetailViewModel.selectedSchoolCode.value = Gson().fromJson(
//            requireArguments().getString(ARG_CONTENT1),
//            SchoolCode::class.java
//        )

        binding.viewModel = salgFormDetailViewModel
        return root
    }

    companion object {
        private const val ARG_CONTENT1 = "content1"
        private const val ARG_CONTENT2 = "projectInfo"
        private const val U_DICE_CODE = "uDiceCode"
        private const val LOCAL_DATA = "localData"

        fun newInstance(content1: String, content2: String, uDiceCode: String?,localData: String?) =
            SalgFormDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONTENT1, content1)
                    putString(ARG_CONTENT2, content2)
                    putString(U_DICE_CODE, uDiceCode)
                    putString(LOCAL_DATA, localData)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if(salgFormDetailViewModel.projectInfo.value?.visit_id !=null)
        {
            getVisitData()
        }
        else{
            salgFormDetailViewModel.visitData.value = GetVisitDataResponseData()
                val requestModel = Gson().fromJson(requireArguments().getString(LOCAL_DATA), RequestModel::class.java)
                salgFormDetailViewModel.visitData.value!!.visit_1 = requestModel.visitData
            if (salgFormDetailViewModel.visitData.value?.visit_1?.visit_image_1?.value != null) {
                loadImage(
                    salgFormDetailViewModel.visitData.value?.visit_1?.visit_image_1!!.value.toString(),
                    binding.img1, binding.llImg1
                )
            }

            if (salgFormDetailViewModel.visitData.value?.visit_1?.visit_image_2?.value != null) {
                loadImage(
                    salgFormDetailViewModel.visitData.value?.visit_1?.visit_image_2!!.value.toString(),
                    binding.img2, binding.llImg2
                )
            }
        }

    }

    override fun onResume() {
        super.onResume()
    }

    private fun visitsDataModel(): RequestModel {
        return salgFormDetailViewModel.projectInfo.value?.visit_id?.let {
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
                salgFormDetailViewModel.visitData.value = Gson().fromJson(
                    model.getJSONObject("data").toString(),
                    GetVisitDataResponseData::class.java
                )
                Log.d("Nitin", requireArguments().getString(LOCAL_DATA)!!)
                if(salgFormDetailViewModel.visitData.value!!.visit_1 == null)
                {
                    val requestModel = Gson().fromJson(requireArguments().getString(LOCAL_DATA), RequestModel::class.java)
                    salgFormDetailViewModel.visitData.value!!.visit_1 = requestModel.visitData
                }
                if (salgFormDetailViewModel.visitData.value?.visit_1?.visit_image_1?.value != null) {
                    loadImage(
                        salgFormDetailViewModel.visitData.value?.visit_1?.visit_image_1!!.value.toString(),
                        binding.img1, binding.llImg1
                    )
                }

                if (salgFormDetailViewModel.visitData.value?.visit_1?.visit_image_2?.value != null) {
                    loadImage(
                        salgFormDetailViewModel.visitData.value?.visit_1?.visit_image_2!!.value.toString(),
                        binding.img2, binding.llImg2
                    )
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

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val decodedByte = withContext(Dispatchers.IO) {
                    val decodedString = Base64.decode(base64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
                }
                val imageUri = Uri.parse(base64)

                if(base64.startsWith("content://")){
                    Glide.with(imgId.context)
                        .load(imageUri)
                        .into(imgId)
                }else{
                    Glide.with(imgId.context)
                        .load(decodedByte)
                        .into(imgId)
                }


//                Glide.with(imgId.context)
//                    .load(decodedByte)
//                    .into(imgId)

                llId.visibility = View.VISIBLE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
//        binding.img1.setImageBitmap(null)
//        binding.img2.setImageBitmap(null)
//        binding.img3.setImageBitmap(null)
//        binding.img4.setImageBitmap(null)
    }
}