package com.hul.screens.field_auditor_dashboard.ui.dashboard

import com.hul.data.MappedUser
import com.hul.data.ProjectInfo

interface DashboardFragmentInterface {

    fun redirectToAttendence(projectInfo : ProjectInfo)
    fun redirectToVisits(mappedUser: MappedUser)
}