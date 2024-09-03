package com.hul.sb.mobiliser.ui.sbform3details

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hul.data.GetVisitDataResponseData
import com.hul.data.ProjectInfo
import com.hul.data.SchoolCode
import com.hul.user.UserInfo
import javax.inject.Inject

class SBForm3DetailsViewModel @Inject constructor(
    private val userInfo: UserInfo,
) : ViewModel() {

    var selectedSchoolCode = MutableLiveData<SchoolCode>()
    var projectInfo = MutableLiveData<ProjectInfo>()

    var visitData = MutableLiveData<GetVisitDataResponseData>(null)

    var uDiceCode = MutableLiveData<String>(null)

    var visibiliyOfItems = MutableLiveData<Int>(View.VISIBLE)
}