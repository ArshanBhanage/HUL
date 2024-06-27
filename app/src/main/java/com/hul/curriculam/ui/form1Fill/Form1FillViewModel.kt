package com.hul.curriculam.ui.form1Fill

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

class Form1FillViewModel @Inject constructor(
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
    var imageUrl4 = MutableLiveData<String>("")

    var imageApiUrl1 = MutableLiveData<String>("")
    var imageApiUrl2 = MutableLiveData<String>("")
    var imageApiUrl3 = MutableLiveData<String>("")
    var imageApiUrl4 = MutableLiveData<String>("")

    val loginEnabled = MediatorLiveData<Boolean>(true)


    val timerFinished = MediatorLiveData<Boolean>(false)

    var form1 = MutableLiveData<String>("")
    var form2 = MutableLiveData<String>("")
    var form3 = MutableLiveData<String>("")
    var form4 = MutableLiveData<String>("")
    var form5 = MutableLiveData<String>("")
    var form6 = MutableLiveData<Boolean>(false)

    var uDiceCode = MutableLiveData<String>(null)

    var noOfBooksHandedOver = MutableLiveData<String>("")

    var teachersTrained = MutableLiveData<String>("")

    val form4Text = MediatorLiveData<String>("Yes")

    val isBookDistributionApproved = MutableLiveData<Int>(0)

    var noOfBooksGivenToSchool = MutableLiveData(0)

    init {

//        form4Text.addSource(form4) { value ->
//            form4Text.value = if (value) "Yes" else "No"
//        }

        loginEnabled.addSource(imageUrl1) {
            loginEnabled.value = it.length > 0 && imageUrl2.value!!.length > 0 && imageUrl3.value!!.length > 0 && imageUrl4.value!!.length > 0 && noOfBooksHandedOver.value!!.length > 0 && teachersTrained.value!!.length > 0  && form1.value!!.length > 0 && form2.value!!.length > 0 && form3.value!!.length > 0 && form4.value!!.length > 0
        }

        loginEnabled.addSource(imageUrl2) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl3.value!!.length > 0 && imageUrl4.value!!.length > 0 && noOfBooksHandedOver.value!!.length > 0 && teachersTrained.value!!.length > 0 && form1.value!!.length > 0 && form2.value!!.length > 0 && form3.value!!.length > 0 && form4.value!!.length > 0
        }

        loginEnabled.addSource(imageUrl3) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl2.value!!.length > 0 && imageUrl4.value!!.length > 0 && noOfBooksHandedOver.value!!.length > 0 && teachersTrained.value!!.length > 0 && form1.value!!.length > 0 && form2.value!!.length > 0 && form3.value!!.length > 0 && form4.value!!.length > 0
        }

        loginEnabled.addSource(imageUrl4) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl2.value!!.length > 0 && imageUrl3.value!!.length > 0 && noOfBooksHandedOver.value!!.length > 0 && teachersTrained.value!!.length > 0 && form1.value!!.length > 0 && form2.value!!.length > 0 && form3.value!!.length > 0 && form4.value!!.length > 0
        }

        loginEnabled.addSource(noOfBooksHandedOver) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl2.value!!.length > 0 && imageUrl3.value!!.length > 0 && imageUrl4.value!!.length > 0 && teachersTrained.value!!.length > 0 && form1.value!!.length > 0 && form2.value!!.length > 0 && form3.value!!.length > 0 && form4.value!!.length > 0
        }

        loginEnabled.addSource(teachersTrained) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl2.value!!.length > 0 && imageUrl3.value!!.length > 0 && imageUrl4.value!!.length > 0 && noOfBooksHandedOver.value!!.length > 0 && form1.value!!.length > 0 && form2.value!!.length > 0 && form3.value!!.length > 0 && form4.value!!.length > 0
        }

        loginEnabled.addSource(form1) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl2.value!!.length > 0 && imageUrl3.value!!.length > 0 && imageUrl4.value!!.length > 0 && noOfBooksHandedOver.value!!.length > 0 && teachersTrained.value!!.length > 0 && form2.value!!.length > 0 && form3.value!!.length > 0 && form4.value!!.length > 0
        }

        loginEnabled.addSource(form2) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl2.value!!.length > 0 && imageUrl3.value!!.length > 0 && imageUrl4.value!!.length > 0 && noOfBooksHandedOver.value!!.length > 0 && teachersTrained.value!!.length > 0  && form1.value!!.length > 0 && form3.value!!.length > 0 && form4.value!!.length > 0
        }

        loginEnabled.addSource(form3) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl2.value!!.length > 0 && imageUrl3.value!!.length > 0 && imageUrl4.value!!.length > 0 && noOfBooksHandedOver.value!!.length > 0 && teachersTrained.value!!.length > 0  && form2.value!!.length > 0 && form2.value!!.length > 0 && form4.value!!.length > 0
        }

        loginEnabled.addSource(form4) {
            loginEnabled.value = it.length > 0 && imageUrl1.value!!.length > 0 && imageUrl2.value!!.length > 0 && imageUrl3.value!!.length > 0 && imageUrl4.value!!.length > 0 && noOfBooksHandedOver.value!!.length > 0 && teachersTrained.value!!.length > 0  && form2.value!!.length > 0 && form2.value!!.length > 0 && form3.value!!.length > 0
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

    val capture4Visibility: LiveData<Int> = imageUrl4.map {
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

    val captured4Visibility: LiveData<Int> = capture4Visibility.map {
        if(it == View.GONE)
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }

    val noOfBooksHandedOverError: LiveData<String> = noOfBooksHandedOver.map {

        if(it.length > 0)
        {
            ""
        }
        else{
            "Enter value"
        }
    }

    val teachersTrainedError: LiveData<String> = teachersTrained.map {

        if(it.length > 0)
        {
            ""
        }
        else{
            "Enter value"
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

        if(it.length > 0 && it.length == 10)
        {
            ""
        }
        else if(it.length==0){
            "Enter mobile number"
        }
        else{
            "Enter correct mobile number"
        }
    }

    val form3Error: LiveData<String> = form3.map {

        if(it.length > 0)
        {
            ""
        }
        else{
            "Enter name"
        }
    }

    val form4Error: LiveData<String> = form4.map {

        if(it.length > 0 && it.length == 10)
        {
            ""
        }
        else if(it.length==0){
            "Enter mobile number"
        }
        else{
            "Enter correct mobile number"
        }
    }

    var visitData = MutableLiveData<GetVisitDataResponseData>(null)
    var visitDataToView = MutableLiveData<Visit1>(null)

}