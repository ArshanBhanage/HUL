package com.hul.sb.mobiliser.ui.sbform2details

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
import com.hul.data.GetVisitDataResponseData
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.databinding.FragmentSBForm2DetailsBinding
import com.hul.databinding.FragmentSBFrom1DetailsBinding
import com.hul.sb.SBDashboardComponent
import com.hul.sb.mobiliser.ui.sbform1details.SBForm1DetailsFragment
import com.hul.sb.mobiliser.ui.sbform1details.SBFrom1DetailsViewModel
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

class SBForm2DetailsFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentSBForm2DetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var sbDashboardComponent: SBDashboardComponent

    @Inject
    lateinit var sbForm2DetailsViewModel: SBForm2DetailsViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSBForm2DetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        sbDashboardComponent =
            (activity?.application as HULApplication).appComponent.sbDashboardComponent()
                .create()
        sbDashboardComponent.inject(this)

        sbForm2DetailsViewModel.projectInfo.value = Gson().fromJson(
            requireArguments().getString(ARG_CONTENT2),
            ProjectInfo::class.java
        )

        binding.viewModel = sbForm2DetailsViewModel
        return root
    }

    companion object {
        private const val ARG_CONTENT1 = "content1"
        private const val ARG_CONTENT2 = "content2"
        private const val U_DICE_CODE = "uDiceCode"
        private const val LOCAL_DATA = "localData"

        fun newInstance(content2: String,localData: String?) =
            SBForm2DetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONTENT2, content2)
                    putString(LOCAL_DATA, localData)
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
        return sbForm2DetailsViewModel.projectInfo.value?.visit_id?.let {
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
                sbForm2DetailsViewModel.visitData.value = Gson().fromJson(
                    model.getJSONObject("data").toString(),
                    GetVisitDataResponseData::class.java
                )
                Log.d("Nitin", requireArguments().getString(LOCAL_DATA)!!)
                if(sbForm2DetailsViewModel.visitData.value!!.visit_2 == null)
                {
                    val requestModel = Gson().fromJson(requireArguments().getString(LOCAL_DATA), RequestModel::class.java)
                    sbForm2DetailsViewModel.visitData.value!!.visit_2 = requestModel.visitData
                }
                if (sbForm2DetailsViewModel.visitData.value?.visit_2?.visit_image_1?.value != null) {
                    loadImage(
                        sbForm2DetailsViewModel.visitData.value?.visit_2?.visit_image_1!!.value.toString(),
                        binding.img1, binding.llImg1
                    )
                }

                if (sbForm2DetailsViewModel.visitData.value?.visit_2?.visit_image_2?.value != null) {
                    loadImage(
                        sbForm2DetailsViewModel.visitData.value?.visit_2?.visit_image_2!!.value.toString(),
                        binding.img2, binding.llImg2
                    )
                }
                Log.d("sbForm2DetailsViewModel.visitData", "onApiSuccess: ${sbForm2DetailsViewModel.visitData.value?.visit_2?.visit_image_3?.value}")

                if (sbForm2DetailsViewModel.visitData.value?.visit_2?.visit_image_3?.value != null) {
                    loadImage(
                        sbForm2DetailsViewModel.visitData.value?.visit_2?.visit_image_3!!.value.toString(),
                        binding.img3, binding.llImg3
                    )
                }

                if (sbForm2DetailsViewModel.visitData.value?.visit_2?.visit_image_4?.value != null) {
                    loadImage(
                        sbForm2DetailsViewModel.visitData.value?.visit_2?.visit_image_4!!.value.toString(),
                        binding.img4, binding.llImg4
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
        binding.img1.setImageBitmap(null)
        binding.img2.setImageBitmap(null)
        binding.img3.setImageBitmap(null)
        binding.img4.setImageBitmap(null)
    }

}