package com.hul.curriculam.ui.formDetails

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hul.data.GetVisitDataResponseData
import com.hul.data.ProjectInfo
import com.hul.data.SchoolCode
import com.hul.data.Visit1
import com.hul.user.UserInfo
import javax.inject.Inject

class FormViewModel   @Inject constructor(
    private val userInfo: UserInfo,
)  : ViewModel() {

    var selectedSchoolCode = MutableLiveData<SchoolCode>()
    var projectInfo = MutableLiveData<ProjectInfo>()

    var visitData = MutableLiveData<GetVisitDataResponseData>(null)

    var visitDataToView = MutableLiveData<Visit1>(null)
}