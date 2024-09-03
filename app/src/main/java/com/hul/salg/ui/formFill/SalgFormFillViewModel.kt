package com.hul.salg.ui.formFill

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.hul.data.Society
import com.hul.user.UserInfo
import javax.inject.Inject

class SalgFormFillViewModel @Inject constructor(
    private val userInfo: UserInfo,
)  : ViewModel() {

    var imageUrl1 = MutableLiveData<String>("")
    var imageUrl2 = MutableLiveData<String>("")

    var responseModel = MutableLiveData<String>("")
    var speakWithModel = MutableLiveData<String>("")
    var consentModel = MutableLiveData<String>("")
    var familyAwarenessModel = MutableLiveData<String>("")
    var howManyCategories = MutableLiveData<String>("")
    var currentlySegregateWaste = MutableLiveData<String>("")
    var housekeepingStaffCollect = MutableLiveData<String>("")
    var haveAChampion = MutableLiveData<String>("")
    var name = MutableLiveData<String>("")
    var phoneNumber = MutableLiveData<String>("")
    var support = MutableLiveData<String>("")
    var yourExperience = MutableLiveData<String>("")

    var responseModelError = MutableLiveData<String>("")
    var speakWithModelError = MutableLiveData<String>("")
    var consentModelError = MutableLiveData<String>("")
    var familyAwarenessModelError = MutableLiveData<String>("")
    var howManyCategoriesError = MutableLiveData<String>("")
    var currentlySegregateWasteError = MutableLiveData<String>("")
    var housekeepingStaffCollectError = MutableLiveData<String>("")
    var haveAChampionError = MutableLiveData<String>("")
    var nameError = MutableLiveData<String>("")
    var phoneNumberError = MutableLiveData<String>("")
    var supportError = MutableLiveData<String>("")
    var yourExperienceError = MutableLiveData<String>("")

    var responseVisibility : LiveData<Int> = responseModel.map {
        if(it.equals("Accepted",true))
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }

    var participataionVisibility : LiveData<Int> = consentModel.map {
        if(it.equals("Yes",true))
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }

    var championVisibility : LiveData<Int> = haveAChampion.map {
        if(it.equals("Yes",true))
        {
            View.VISIBLE
        }
        else{
            View.GONE
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

    val captured1Visibility: LiveData<Int> = capture1Visibility.map {
        if(it == View.GONE)
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }

    var projectInfo = MutableLiveData<Society>()
    var wingNumber = MutableLiveData<String>("")
    var floor = MutableLiveData<String>("")
    var flatNumber = MutableLiveData<String>("")

    val capture2Visibility: LiveData<Int> = imageUrl2.map {
        if(it.length>0)
        {
            View.GONE
        }
        else{
            View.VISIBLE
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
}