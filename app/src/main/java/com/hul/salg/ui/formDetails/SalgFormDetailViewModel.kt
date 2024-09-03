package com.hul.salg.ui.formDetails

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.hul.data.GetVisitDataResponseData
import com.hul.data.ProjectInfo
import com.hul.user.UserInfo
import javax.inject.Inject

class SalgFormDetailViewModel @Inject constructor(
    private val userInfo: UserInfo,
) : ViewModel() {

    var projectInfo = MutableLiveData<ProjectInfo>()

    var visitData = MutableLiveData<GetVisitDataResponseData>(null)

    var responseVisibility : LiveData<Int> = visitData.map {
        if(it != null && it.visit_1!!.response!!.value!!.toString().equals("Accepted",true))
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }

    var participataionVisibility : LiveData<Int> = visitData.map {
        if(it != null && it.visit_1!!.did_you_take_consent_for_their_participation!!.value!!.toString().equals("Yes",true))
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }

    var championVisibility : LiveData<Int> = visitData.map {
        if(it != null && it.visit_1!!.does_this_household_have_a_champion!!.value!!.toString().equals("Yes",true))
        {
            View.VISIBLE
        }
        else{
            View.GONE
        }
    }
}