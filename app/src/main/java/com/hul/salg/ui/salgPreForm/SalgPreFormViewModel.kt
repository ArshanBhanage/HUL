package com.hul.salg.ui.salgPreForm

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.hul.data.Society
import com.hul.user.UserInfo
import javax.inject.Inject

class SalgPreFormViewModel @Inject constructor(
    private val userInfo: UserInfo,
)  : ViewModel() {

    var projectInfo = MutableLiveData<Society>()

    var imageUrl1 = MutableLiveData<String>("")

    var responseModel = MutableLiveData<String>("")

    var wingNumber = MutableLiveData<String>("")
    var floor = MutableLiveData<String>("")
    var flatNumber = MutableLiveData<String>("")
    var anotherFlatNumber = MutableLiveData<String>("")
    var date = MutableLiveData<String>("")
    var time = MutableLiveData<String>("")

    var responseModelError = MutableLiveData<String>("")
    var floorError = MutableLiveData<String>("")
    var wingNumberError = MutableLiveData<String>("")
    var flatNumberError = MutableLiveData<String>("")
    var anotherFlatNumberError = MutableLiveData<String>("")
    var dateError = MutableLiveData<String>("")
    var timeError = MutableLiveData<String>("")

    var responseVisibility : LiveData<Int> = responseModel.map {
        if(it.equals("Come back later",true))
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }

    var responseVisibility1 : LiveData<Int> = responseModel.map {
        if(it.equals("Link to another apartment",true))
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
}