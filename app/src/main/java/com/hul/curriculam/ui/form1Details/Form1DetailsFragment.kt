package com.hul.curriculam.ui.form1Details

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
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
import com.hul.data.VisitData
import com.hul.databinding.FragmentForm1Binding
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
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class Form1DetailsFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentForm1Binding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var curriculamComponent: CurriculamComponent

    @Inject
    lateinit var form1ViewModel: Form1ViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentForm1Binding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        curriculamComponent =
            (activity?.application as HULApplication).appComponent.curriculamComponent()
                .create()
        curriculamComponent.inject(this)

        form1ViewModel.selectedSchoolCode.value = Gson().fromJson(
            requireArguments().getString(ARG_CONTENT1),
            SchoolCode::class.java
        )

        form1ViewModel.projectInfo.value = Gson().fromJson(
            requireArguments().getString(ARG_CONTENT2),
            ProjectInfo::class.java
        )

        form1ViewModel.uDiceCode.value = requireArguments().getString(U_DICE_CODE)

        binding.viewModel = form1ViewModel
        return root
    }

    companion object {
        private const val ARG_CONTENT1 = "content1"
        private const val ARG_CONTENT2 = "content2"
        private const val U_DICE_CODE = "uDiceCode"
        private const val LOCAL_DATA = "localData"

        fun newInstance(content1: String, content2: String, uDiceCode: String?,localData: String?) =
            Form1DetailsFragment().apply {
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
        getVisitData()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun visitsDataModel(): RequestModel {
        return form1ViewModel.projectInfo.value?.visit_id?.let {
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
                form1ViewModel.visitData.value = Gson().fromJson(
                    model.getJSONObject("data").toString(),
                    GetVisitDataResponseData::class.java
                )
                Log.d("Nitin", requireArguments().getString(LOCAL_DATA)!!)
                if(form1ViewModel.visitData.value!!.visit_1 == null)
                {
                    val requestModel = Gson().fromJson(requireArguments().getString(LOCAL_DATA), RequestModel::class.java)
                    form1ViewModel.visitData.value!!.visit_1 = requestModel.visitData
                }
                form1ViewModel.visibiliyOfItems.value = if(form1ViewModel.visitData.value?.visit_1?.school_closed!!.value.toString().toBoolean()) View.GONE else View.VISIBLE
                form1ViewModel.uDiceCode.value = form1ViewModel.visitData.value?.visit_1?.u_dice_code!!.value.toString()
                if (form1ViewModel.visitData.value?.visit_1?.visit_image_1?.value != null) {
                    loadImage(
                        form1ViewModel.visitData.value?.visit_1?.visit_image_1!!.value.toString(),
                        binding.img1, binding.llImg1
                    )
                }

                if (form1ViewModel.visitData.value?.visit_1?.visit_image_2?.value != null) {
                    loadImage(
                        form1ViewModel.visitData.value?.visit_1?.visit_image_2!!.value.toString(),
                        binding.img2, binding.llImg2
                    )
                }
                Log.d("form1ViewModel.visitData", "onApiSuccess: ${form1ViewModel.visitData.value?.visit_1?.visit_image_3?.value}")

                if (form1ViewModel.visitData.value?.visit_1?.visit_image_3?.value != null) {
                    loadImage(
                        form1ViewModel.visitData.value?.visit_1?.visit_image_3!!.value.toString(),
                        binding.img3, binding.llImg3
                    )
                }

                if (form1ViewModel.visitData.value?.visit_1?.visit_image_4?.value != null) {
                    loadImage(
                        form1ViewModel.visitData.value?.visit_1?.visit_image_4!!.value.toString(),
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