package com.hul.sb.supervisor.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hul.data.Attendencemodel
import com.hul.user.UserInfo
import javax.inject.Inject

/**
 * Created by Nitin Chorge on 02-09-2024.
 */
class SBSupervisorDashboardViewModel @Inject constructor(
    private val userInfo: UserInfo,
)  : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is dashboard Fragment"
    }
    val text: LiveData<String> = _text

    val attendenceToday = MutableLiveData<Attendencemodel>()

    val userName: LiveData<String> = MutableLiveData(userInfo.projectName)
    val userType: LiveData<String> = MutableLiveData(userInfo.userType)
}