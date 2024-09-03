package com.hul.sb.mobiliser.ui.sbform3fill

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.hul.data.GetVisitDataResponseData
import com.hul.data.ProjectInfo
import com.hul.data.SchoolCode
import com.hul.data.Visit1
import com.hul.user.UserInfo
import javax.inject.Inject

class SBForm3FillViewModel  @Inject constructor(
    private val userInfo: UserInfo,
)  : ViewModel() {

    var selectedSchoolCode = MutableLiveData<SchoolCode>()

    var projectInfo = MutableLiveData<ProjectInfo>()

    var position = MutableLiveData<Int>(0)

    var numberOfStudentEditable = MutableLiveData(false)

    var longitude = MutableLiveData<String>()
    var lattitude = MutableLiveData<String>()

    var imageUrl1 = MutableLiveData<String>("")
    var imageUrl2 = MutableLiveData<String>("")
    var imageUrl3 = MutableLiveData<String>("")

    var imageApiUrl1 = MutableLiveData<String>("")
    var imageApiUrl2 = MutableLiveData<String>("")
    var imageApiUrl3 = MutableLiveData<String>("")
    var imageApiUrl4 = MutableLiveData<String>("")

    val loginEnabled = MediatorLiveData<Boolean>(true)



    val timerFinished = MediatorLiveData<Boolean>(false)

    var houseCode = MutableLiveData<String>("")
    var customerName = MutableLiveData<String>("")
    var hasSuvidhaPass = MutableLiveData<String>("")
    var leadQuality = MutableLiveData<String>("")


    init {

        loginEnabled.addSource(imageUrl1) {
            loginEnabled.value = it.length > 0 &&
                    imageUrl2.value!!.length > 0 &&
                    imageUrl3.value!!.length > 0 &&
                    houseCode.value!!.length > 0 &&
                    customerName.value!!.length > 0  &&
                    hasSuvidhaPass.value!!.length > 0 &&
                    
                    leadQuality.value!!.length > 0
        }

        loginEnabled.addSource(imageUrl2) {
            loginEnabled.value = it.length > 0 &&
                    imageUrl1.value!!.length > 0 &&
                    imageUrl3.value!!.length > 0 &&
                    houseCode.value!!.length > 0 &&
                    customerName.value!!.length > 0  &&
                    hasSuvidhaPass.value!!.length > 0 &&
                    
                    leadQuality.value!!.length > 0
        }

        loginEnabled.addSource(imageUrl3) {
            loginEnabled.value = it.length > 0 &&
                    imageUrl2.value!!.length > 0 &&
                    imageUrl1.value!!.length > 0 &&
                    houseCode.value!!.length > 0 &&
                    customerName.value!!.length > 0  &&
                    hasSuvidhaPass.value!!.length > 0 &&
                    
                    leadQuality.value!!.length > 0
        }

        loginEnabled.addSource(houseCode) {
            loginEnabled.value = it.length > 0 &&
                    imageUrl1.value!!.length > 0 &&
                    imageUrl2.value!!.length > 0 &&
                    imageUrl3.value!!.length > 0 &&
                    customerName.value!!.length > 0  &&
                    hasSuvidhaPass.value!!.length > 0 &&
                    
                    leadQuality.value!!.length > 0
        }

        loginEnabled.addSource(customerName) {
            loginEnabled.value = it.length > 0 &&
                    imageUrl1.value!!.length > 0 &&
                    imageUrl2.value!!.length > 0 &&
                    imageUrl3.value!!.length > 0 &&
                    houseCode.value!!.length > 0  &&
                    hasSuvidhaPass.value!!.length > 0 &&
                    
                    leadQuality.value!!.length > 0
        }

        loginEnabled.addSource(hasSuvidhaPass) {
            loginEnabled.value = it.length > 0 &&
                    imageUrl1.value!!.length > 0 &&
                    imageUrl2.value!!.length > 0 &&
                    imageUrl3.value!!.length > 0 &&
                    customerName.value!!.length > 0  &&
                    houseCode.value!!.length > 0 &&
                    
                    leadQuality.value!!.length > 0
        }



        loginEnabled.addSource(leadQuality) {
            loginEnabled.value = it.length > 0 &&
                    imageUrl1.value!!.length > 0 &&
                    imageUrl2.value!!.length > 0 &&
                    imageUrl3.value!!.length > 0 &&
                    customerName.value!!.length > 0  &&
                    hasSuvidhaPass.value!!.length > 0 &&
                    
                    houseCode.value!!.length > 0
        }

    }

    val capture1Visibility: LiveData<Int> = imageUrl1.map {
        if(it.length>0)
        {
            View.GONE
        }
        else{
            View.VISIBLE
        }
    }

    val capture2Visibility: LiveData<Int> = imageUrl2.map {
        if(it.length>0)
        {
            View.GONE
        }
        else{
            View.VISIBLE
        }
    }

    val capture3Visibility: LiveData<Int> = imageUrl3.map {
        if(it.length>0)
        {
            View.GONE
        }
        else{
            View.VISIBLE
        }
    }

    val captured1Visibility: LiveData<Int> = capture1Visibility.map {
        if(it == View.GONE)
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }

    val captured2Visibility: LiveData<Int> = capture2Visibility.map {
        if(it == View.GONE)
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }

    val captured3Visibility: LiveData<Int> = capture3Visibility.map {
        if(it == View.GONE)
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }

    var houseCodeError : LiveData<String> = houseCode.map {

        if(it.length > 0)
        {
            ""
        }
        else{
            "Enter value"
        }
    }
    var customerNameError : LiveData<String> = customerName.map {

        if(it.length > 0)
        {
            ""
        }
        else{
            "Enter value"
        }
    }

    var hasSuvidhaPassError : LiveData<String> = hasSuvidhaPass.map {

        if(it.length > 0)
        {
            ""
        }
        else{
            "Enter value"
        }
    }

    var leadQualityError : LiveData<String> = leadQuality.map {

        if(it.length > 0)
        {
            ""
        }
        else{
            "Enter value"
        }
    }

    var visitData = MutableLiveData<GetVisitDataResponseData>(null)
    var visitDataToView = MutableLiveData<Visit1>(null)

}