package com.hul.curriculam.ui.schoolCode

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hul.data.SchoolCode
import com.hul.user.UserInfo
import javax.inject.Inject

class SchoolCodeViewModel  @Inject constructor(
    private val userInfo: UserInfo,
)  : ViewModel() {

    var selectedSchoolCode = MutableLiveData<SchoolCode>()
}