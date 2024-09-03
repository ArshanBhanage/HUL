package com.hul.salg.ui.salgForm

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hul.data.ProjectInfo
import com.hul.data.SchoolCode
import com.hul.data.Society
import com.hul.user.UserInfo
import javax.inject.Inject

class SalgFormViewModel @Inject constructor(
    private val userInfo: UserInfo,
)  : ViewModel() {

    var projectInfo = MutableLiveData<Society>()
//    var selectedSchoolCode = MutableLiveData<SchoolCode>()
//    var visitList = MutableLiveData<ArrayList<ProjectInfo>>()
//    var projectInfo = MutableLiveData<ProjectInfo>()
}