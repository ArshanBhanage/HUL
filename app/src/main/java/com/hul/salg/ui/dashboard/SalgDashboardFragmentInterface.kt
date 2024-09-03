package com.hul.salg.ui.dashboard

import com.hul.data.ProjectInfo
import com.hul.data.Society
import com.hul.sync.SocietyVisitDataTable

interface SalgDashboardFragmentInterface {

    fun redirectToAttendence(projectInfo : Society)
    fun redirectToDetails(projectInfo : Society)
}