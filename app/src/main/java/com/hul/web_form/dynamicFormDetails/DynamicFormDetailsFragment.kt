package com.hul.web_form.dynamicFormDetails

import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.hul.HULApplication
import com.hul.R
import com.hul.api.ApiExtentions
import com.hul.api.ApiHandler
import com.hul.api.controller.APIController
import com.hul.data.FormElement
import com.hul.data.GetVisitDataResponseData
import com.hul.data.ProjectInfo
import com.hul.data.RequestModel
import com.hul.databinding.FragmentDynamicFormDetailsBinding
import com.hul.user.UserInfo
import com.hul.utils.ConnectionDetector
import com.hul.utils.RetryInterface
import com.hul.utils.cancelProgressDialog
import com.hul.utils.noInternetDialogue
import com.hul.utils.redirectionAlertDialogue
import com.hul.utils.setProgressDialog
import com.hul.web_form.WebFormComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import javax.inject.Inject


class DynamicFormDetailsFragment : Fragment(), ApiHandler, RetryInterface {

    private var _binding: FragmentDynamicFormDetailsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var webFormComponent: WebFormComponent

    @Inject
    lateinit var dynamicFormDetailsViewModel: DynamicFormDetailsViewModel

    @Inject
    lateinit var userInfo: UserInfo

    @Inject
    lateinit var apiController: APIController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDynamicFormDetailsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.lifecycleOwner = viewLifecycleOwner
        webFormComponent =
            (activity?.application as HULApplication).appComponent.webFormComponent()
                .create()
        webFormComponent.inject(this)


        dynamicFormDetailsViewModel.projectInfo.value = Gson().fromJson(
            requireArguments().getString(ARG_CONTENT1),
            ProjectInfo::class.java
        )

        binding.viewModel = dynamicFormDetailsViewModel
        return root
    }

    companion object {
        private const val ARG_CONTENT1 = "content1"
        private const val ARG_CONTENT2 = "content2"
        private const val U_DICE_CODE = "uDiceCode"
        private const val LOCAL_DATA = "localData"

        fun newInstance(content1: String) =
            DynamicFormDetailsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CONTENT1, content1)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getVisitData(false)
    }

    override fun onResume() {
        super.onResume()
    }

    private fun visitsDataModel(flag : Boolean): RequestModel {
        return dynamicFormDetailsViewModel.projectInfo.value?.visit_id?.let {
            RequestModel(
                project = userInfo.projectName,
                visitId = it,
                loadImages = flag
            )
        }!!
    }

    private fun getVisitData(flag : Boolean) {
        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            setProgressDialog(requireContext(), "Loading Visit data")
            apiController.getApiResponse(
                this,
                visitsDataModel(flag),
                ApiExtentions.ApiDef.GET_VISIT_DATA.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.GET_VISIT_DATA.ordinal, this)
        }
    }

    private fun getVisitDataImage() {
        if (ConnectionDetector(requireContext()).isConnectingToInternet()) {
            setProgressDialog(requireContext(), "Loading Visit data")
            apiController.getApiResponse(
                this,
                visitsDataModel(true),
                ApiExtentions.ApiDef.GET_VISIT_DATA_IMAGE.ordinal
            )
        } else {
            noInternetDialogue(requireContext(), ApiExtentions.ApiDef.GET_VISIT_DATA.ordinal, this)
        }
    }

    private fun displayFormData(data: JSONObject) {
        val keyList = getKeyNamesFromJsonObject(data)
        val array = JSONArray()
        val arrayString = arrayListOf<String>()
        for (element in keyList) {
            if (data.getJSONObject(element).getBoolean("is_image")) {
                array.put(data.getJSONObject(element))
                arrayString.add(element)
            } else if(element != "null"){
                inflateSpinnerLayout(data.getJSONObject(element), element)
            }
        }
//        for (i in 0 until array.length() step 2) {
//            if (i + 1 < array.length()) {
//                val firstValue = array.getJSONObject(i)
//                val secondValue = array.getJSONObject(i + 1)
//                val firstString = arrayString.get(i)
//                val secondString = arrayString.get(i + 1)
//                inflateImageLayout(firstValue,secondValue,firstString,secondString)
//            } else {
//                // Handle the case where there's an odd number of elements
//                val firstValue = array.getJSONObject(i)
//                val secondValue = JSONObject()
//                val firstString = arrayString.get(i)
//                val secondString = ""
//                inflateImageLayout(firstValue,secondValue,firstString,secondString)
//            }
//        }

        getVisitDataImage()

    }

    private fun displayImages(data: JSONObject) {
        val keyList = getKeyNamesFromJsonObject(data)
        val array = JSONArray()
        val arrayString = arrayListOf<String>()
        for (element in keyList) {
            if (data.getJSONObject(element).getBoolean("is_image")) {
                array.put(data.getJSONObject(element))
                arrayString.add(element)
            }
        }

            val view = layoutInflater.inflate(R.layout.display_image_header, binding.formContainer, false)
            binding.formContainer.addView(view)

        for (i in 0 until array.length() step 2) {
            if (i + 1 < array.length()) {
                val firstValue = array.getJSONObject(i)
                val secondValue = array.getJSONObject(i + 1)
                val firstString = arrayString.get(i)
                val secondString = arrayString.get(i + 1)
                inflateImageLayout(firstValue,secondValue,firstString,secondString)
            } else {
                // Handle the case where there's an odd number of elements
                val firstValue = array.getJSONObject(i)
                val secondValue = JSONObject()
                val firstString = arrayString.get(i)
                val secondString = ""
                inflateImageLayout(firstValue,secondValue,firstString,secondString)
            }
        }

    }

    private fun inflateImageLayout(element1: JSONObject, element2: JSONObject,firstString : String,secondString : String) {
        val view = layoutInflater.inflate(R.layout.display_image_layout, binding.formContainer, false)
        view.findViewById<TextView>(R.id.img1Desc).text = getTag(firstString)

        loadImage(element1.getString(("value")), view.findViewById<ImageView>(R.id.img1) ,view.findViewById<LinearLayout>(R.id.llImg1))

        if(secondString.length > 0)
        {
            view.findViewById<TextView>(R.id.img2Desc).text = getTag(secondString)
            loadImage(element2.getString(("value")), view.findViewById<ImageView>(R.id.img2) ,view.findViewById<LinearLayout>(R.id.llImg2))
        }

        binding.formContainer.addView(view)
    }

    private fun inflateSpinnerLayout(element: JSONObject, key: String) {
        val view = layoutInflater.inflate(R.layout.display_text_file, binding.formContainer, false)
        view.findViewById<TextView>(R.id.header).text = getTag(key)
        view.findViewById<TextView>(R.id.data).text = element.getString("value")

        binding.formContainer.addView(view)
    }

    private fun getTag(string : String) : String
    {
        val output =  string.replace("_", " ")

        return output
            .split(' ')
            .joinToString(" ") { it.replaceFirstChar(Char::uppercaseChar) }
    }

    fun getKeyNamesFromJsonObject(jsonObject: JSONObject): List<String> {
        val namesList: MutableList<String> = ArrayList()
        try {
            val stringIterator = jsonObject.keys()
            while (stringIterator.hasNext()) {
                namesList.add(stringIterator.next())
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return namesList
    }

    override fun onApiSuccess(o: String?, objectType: Int) {
        when (ApiExtentions.ApiDef.entries[objectType]) {

            ApiExtentions.ApiDef.GET_VISIT_DATA -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                dynamicFormDetailsViewModel.visitData.value = Gson().fromJson(
                    model.getJSONObject("data").toString(),
                    GetVisitDataResponseData::class.java
                )
                if (dynamicFormDetailsViewModel.projectInfo.value?.visit_number!!.toInt() == 1) {
                    val data = model.getJSONObject("data").getJSONObject("visit_1")
                    displayFormData(data)
                } else if (dynamicFormDetailsViewModel.projectInfo.value?.visit_number!!.toInt() == 2) {
                    val data = model.getJSONObject("data").getJSONObject("visit_2")
                    displayFormData(data)
                } else if (dynamicFormDetailsViewModel.projectInfo.value?.visit_number!!.toInt() == 3) {
                    val data = model.getJSONObject("data").getJSONObject("visit_3")
                    displayFormData(data)
                }
            }

            ApiExtentions.ApiDef.GET_VISIT_DATA_IMAGE -> {
                cancelProgressDialog()
                val model = JSONObject(o.toString())
                dynamicFormDetailsViewModel.visitData.value = Gson().fromJson(
                    model.getJSONObject("data").toString(),
                    GetVisitDataResponseData::class.java
                )
                if (dynamicFormDetailsViewModel.projectInfo.value?.visit_number!!.toInt() == 1) {
                    val data = model.getJSONObject("data").getJSONObject("visit_1")
                    displayImages(data)
                } else if (dynamicFormDetailsViewModel.projectInfo.value?.visit_number!!.toInt() == 2) {
                    val data = model.getJSONObject("data").getJSONObject("visit_2")
                    displayImages(data)
                } else if (dynamicFormDetailsViewModel.projectInfo.value?.visit_number!!.toInt() == 3) {
                    val data = model.getJSONObject("data").getJSONObject("visit_3")
                    displayImages(data)
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

                if (base64.startsWith("content://")) {
                    Glide.with(imgId.context)
                        .load(imageUri)
                        .into(imgId)
                } else {
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
    }
}