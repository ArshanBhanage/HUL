package com.hul.sb.supervisor.ui.visits

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hul.data.Attendencemodel
import com.hul.data.MappedUser
import com.hul.data.ProjectInfo
import com.hul.user.UserInfo
import javax.inject.Inject

class SupervisorVisitsViewModel @Inject constructor(
    private val userInfo: UserInfo,
) : ViewModel() {

    var position = MutableLiveData<Int>(0)

    var longitude = MutableLiveData<String>()
    var lattitude = MutableLiveData<String>()

    var projectInfo = MutableLiveData<ProjectInfo>()
    var mobiliserUser = MutableLiveData<MappedUser>()
    val attendenceToday = MutableLiveData<Attendencemodel>()
    var remark = MutableLiveData<String>("")

    var pendingSelected = MutableLiveData<Boolean>(true)

    var visitList = MutableLiveData<ArrayList<ProjectInfo>>(ArrayList())

    init {
    }
}