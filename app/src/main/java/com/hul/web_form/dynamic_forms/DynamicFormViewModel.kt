package com.hul.web_form.dynamic_forms

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hul.data.ProjectInfo
import com.hul.data.SchoolCode
import com.hul.user.UserInfo
import javax.inject.Inject

class DynamicFormViewModel @Inject constructor(
    private val userInfo: UserInfo,
)  : ViewModel() {

    var selectedSchoolCode = MutableLiveData<SchoolCode>()
    var visitList = MutableLiveData<ArrayList<ProjectInfo>>()
    var projectInfo = MutableLiveData<ProjectInfo>()


}