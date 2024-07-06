package com.hul.curriculam.ui.form1Details

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hul.data.GetVisitDataResponseData
import com.hul.data.ProjectInfo
import com.hul.data.SchoolCode
import com.hul.data.Visit1
import com.hul.user.UserInfo
import javax.inject.Inject

class Form1ViewModel @Inject constructor(
    private val userInfo: UserInfo,
) : ViewModel() {

    var selectedSchoolCode = MutableLiveData<SchoolCode>()
    var projectInfo = MutableLiveData<ProjectInfo>()

    var visitData = MutableLiveData<GetVisitDataResponseData>(null)

    var uDiceCode = MutableLiveData<String>(null)

    var visibiliyOfItems = MutableLiveData<Int>(View.VISIBLE)
}