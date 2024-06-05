package com.hul.curriculam.ui.schoolForm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hul.data.ProjectInfo
import com.hul.data.SchoolCode
import com.hul.user.UserInfo
import javax.inject.Inject

class SchoolFormViewModel  @Inject constructor(
    private val userInfo: UserInfo,
)  : ViewModel() {

    var selectedSchoolCode = MutableLiveData<SchoolCode>()
    var visitList = MutableLiveData<ArrayList<ProjectInfo>>()
    var projectInfo = MutableLiveData<ProjectInfo>()
}