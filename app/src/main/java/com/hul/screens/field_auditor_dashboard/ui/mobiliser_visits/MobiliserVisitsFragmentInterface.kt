package com.hul.screens.field_auditor_dashboard.ui.mobiliser_visits

import com.hul.data.MappedUser
import com.hul.data.ProjectInfo

interface MobiliserVisitsFragmentInterface {

    fun redirectToSchoolActivity(projectInfo: ProjectInfo, visitsForSchoolId: ArrayList<ProjectInfo>)

    fun goToMap(projectInfo: ProjectInfo)
}