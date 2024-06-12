package com.hul.screens.field_auditor_dashboard.ui.mobiliser_visits

import android.view.View
import androidx.databinding.BindingAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.hul.data.GetVisitDataResponseData
import com.hul.data.MappedUser
import com.hul.data.ProjectInfo
import com.hul.user.UserInfo
import javax.inject.Inject

class MobiliserVisitsViewModel @Inject constructor(
    private val userInfo: UserInfo,
) : ViewModel() {

    var position = MutableLiveData<Int>(0)

    var longitude = MutableLiveData<String>()
    var lattitude = MutableLiveData<String>()

    var projectInfo = MutableLiveData<ProjectInfo>()
    var mobiliserUser = MutableLiveData<MappedUser>()

    var remark = MutableLiveData<String>("")

    var pendingSelected = MutableLiveData<Boolean>(true)

    var visitList = MutableLiveData<ArrayList<ProjectInfo>>(ArrayList())

    init {
    }
}