package com.hul.curriculam.ui.formFill

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.hul.data.ProjectInfo
import com.hul.data.SchoolCode
import com.hul.loginRegistraion.loginwithpin.InValid
import com.hul.loginRegistraion.loginwithpin.Valid
import com.hul.user.UserInfo
import com.hul.utils.initialLetterValidation
import com.hul.utils.numberValidation
import com.hul.utils.repaetValidation
import javax.inject.Inject

class FormFillViewModel @Inject constructor(
    private val userInfo: UserInfo,
)  : ViewModel() {

    var selectedSchoolCode = MutableLiveData<SchoolCode>()

    var projectInfo = MutableLiveData<ProjectInfo>()

    var position = MutableLiveData<Int>(0)

    var longitude = MutableLiveData<String>()
    var lattitude = MutableLiveData<String>()

    var imageUrl1 = MutableLiveData<String>("")
    var imageUrl2 = MutableLiveData<String>("")
    var imageUrl3 = MutableLiveData<String>("")
    var imageUrl4 = MutableLiveData<String>("")
    var imageUrl5 = MutableLiveData<String>("")

    var imageApiUrl1 = MutableLiveData<String>("")
    var imageApiUrl2 = MutableLiveData<String>("")
    var imageApiUrl3 = MutableLiveData<String>("")
    var imageApiUrl4 = MutableLiveData<String>("")
    var imageApiUrl5 = MutableLiveData<String>("")

    val loginEnabled = MediatorLiveData<Boolean>(true)

    var form1 = MutableLiveData<String>("")
    var form2 = MutableLiveData<String>("")
    var form3 = MutableLiveData<String>("")
    var form4 = MutableLiveData<Boolean>(false)
    var form5 = MutableLiveData<String>("")

    init {
        loginEnabled.addSource(imageUrl1) {
            loginEnabled.value = it.length > 0 && imageUrl2.value!!.length > 0 && imageUrl3.value!!.length > 0 && imageUrl4.value!!.length > 0 && imageUrl5.value!!.length > 0 && form1.value!!.length > 0 && form2.value!!.length > 0 && form3.value!!.length > 0
        }

        loginEnabled.addSource(imageUrl2) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl3.value!!.length > 0 && imageUrl4.value!!.length > 0 && imageUrl5.value!!.length > 0 && form1.value!!.length > 0 && form2.value!!.length > 0 && form3.value!!.length > 0
        }

        loginEnabled.addSource(imageUrl3) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl2.value!!.length > 0 && imageUrl4.value!!.length > 0 && imageUrl5.value!!.length > 0 && form1.value!!.length > 0 && form2.value!!.length > 0 && form3.value!!.length > 0
        }

        loginEnabled.addSource(imageUrl4) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl2.value!!.length > 0 && imageUrl3.value!!.length > 0 && imageUrl5.value!!.length > 0 && form1.value!!.length > 0 && form2.value!!.length > 0 && form3.value!!.length > 0
        }

        loginEnabled.addSource(imageUrl5) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl2.value!!.length > 0 && imageUrl3.value!!.length > 0 && imageUrl4.value!!.length > 0 && form1.value!!.length > 0 && form2.value!!.length > 0 && form3.value!!.length > 0
        }

        loginEnabled.addSource(form1) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl2.value!!.length > 0 && imageUrl3.value!!.length > 0 && imageUrl4.value!!.length > 0 && imageUrl5.value!!.length > 0 && form2.value!!.length > 0 && form3.value!!.length > 0
        }

        loginEnabled.addSource(form2) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl2.value!!.length > 0 && imageUrl3.value!!.length > 0 && imageUrl4.value!!.length > 0 && imageUrl5.value!!.length > 0 && form1.value!!.length > 0 && form3.value!!.length > 0
        }

        loginEnabled.addSource(form3) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl2.value!!.length > 0 && imageUrl3.value!!.length > 0 && imageUrl4.value!!.length > 0 && imageUrl5.value!!.length > 0 && form2.value!!.length > 0 && form2.value!!.length > 0
        }

    }

    val capture1Visibility: LiveData<Int> = imageUrl1.map {
        if(it.length>0)
        {
            View.INVISIBLE
        }
        else{
            View.VISIBLE
        }
    }

    val capture2Visibility: LiveData<Int> = imageUrl2.map {
        if(it.length>0)
        {
            View.INVISIBLE
        }
        else{
            View.VISIBLE
        }
    }

    val capture3Visibility: LiveData<Int> = imageUrl3.map {
        if(it.length>0)
        {
            View.INVISIBLE
        }
        else{
            View.VISIBLE
        }
    }

    val capture4Visibility: LiveData<Int> = imageUrl4.map {
        if(it.length>0)
        {
            View.INVISIBLE
        }
        else{
            View.VISIBLE
        }
    }

    val capture5Visibility: LiveData<Int> = imageUrl5.map {
        if(it.length>0)
        {
            View.INVISIBLE
        }
        else{
            View.VISIBLE
        }
    }

    val captured1Visibility: LiveData<Int> = capture1Visibility.map {
        if(it == View.INVISIBLE)
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }

    val captured2Visibility: LiveData<Int> = capture2Visibility.map {
        if(it == View.INVISIBLE)
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }

    val captured3Visibility: LiveData<Int> = capture3Visibility.map {
        if(it == View.INVISIBLE)
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }

    val captured4Visibility: LiveData<Int> = capture4Visibility.map {
        if(it == View.INVISIBLE)
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }

    val captured5Visibility: LiveData<Int> = capture5Visibility.map {
        if(it == View.INVISIBLE)
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }



    val form1Error: LiveData<String> = form1.map {

        if(it.length > 0)
        {
            ""
        }
        else{
            "Enter name"
        }
    }

    val form2Error: LiveData<String> = form2.map {

        if(it.length > 0)
        {
            ""
        }
        else{
            "Enter mobile number"
        }
    }

    val form3Error: LiveData<String> = form3.map {

        if(it.length > 0)
        {
            ""
        }
        else{
            "Enter filled trackers collected"
        }
    }

}