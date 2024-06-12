package com.hul.screens.field_auditor_dashboard.ui.school_activity

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.hul.data.GetVisitDataResponseData
import com.hul.data.ProjectInfo
import com.hul.user.UserInfo
import javax.inject.Inject

class SchoolActivityViewModel @Inject constructor(
    private val userInfo: UserInfo,
) : ViewModel() {

    var imageUrl1 = MutableLiveData<String>("")
    var imageUrl2 = MutableLiveData<String>("")
    var imageUrl3 = MutableLiveData<String>("")

    var imageUrl1API = MutableLiveData<String>("")
    var imageUrl2API = MutableLiveData<String>("")
    var imageUrl3API = MutableLiveData<String>("")

    var longitude = MutableLiveData<String>()
    var lattitude = MutableLiveData<String>()

    var imageType1 = MutableLiveData("Back")
    var imageType2 = MutableLiveData("Image Capture Front")
    var imageType3 = MutableLiveData("Back")

    var projectInfo = MutableLiveData<ProjectInfo>()

    var visitData = MutableLiveData<GetVisitDataResponseData>(null)

    var remark = MutableLiveData<String>("")

    val buttonEnabled = MediatorLiveData<Boolean>(false)

    val noOfBooksGivenToSchool = MutableLiveData<String>("")

    val isBookDistributionApproved = MutableLiveData<Int>(0)

    val capture1Visibility: LiveData<Int> = imageUrl1.map {
        if (it.isNotEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    val capture2Visibility: LiveData<Int> = imageUrl2.map {
        if (it.isNotEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    val capture3Visibility: LiveData<Int> = imageUrl3.map {
        if (it.isNotEmpty()) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    val captured1Visibility: LiveData<Int> = capture1Visibility.map {
        if (it == View.GONE) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    val captured2Visibility: LiveData<Int> = capture2Visibility.map {
        if (it == View.GONE) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    val captured3Visibility: LiveData<Int> = capture3Visibility.map {
        if (it == View.GONE) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

    init {
        buttonEnabled.addSource(imageUrl1) {
            buttonEnabled.value = imageUrl1.value!!.isNotEmpty() && it.isNotEmpty()
        }

        buttonEnabled.addSource(imageUrl2) {
            buttonEnabled.value = imageUrl2.value!!.isNotEmpty() && it.isNotEmpty()
        }

        buttonEnabled.addSource(imageUrl3) {
            buttonEnabled.value = imageUrl3.value!!.isNotEmpty() && it.isNotEmpty()
        }

    }
}