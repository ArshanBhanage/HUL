package com.hul.screens.field_auditor_dashboard

import com.hul.di.ActivityScope
import com.hul.screens.field_auditor_dashboard.ui.attendence.AttendenceFragment
import com.hul.screens.field_auditor_dashboard.ui.dashboard.DashboardFragment
import com.hul.screens.field_auditor_dashboard.ui.mobiliser_visits.MobiliserVisitsFragment
import com.hul.screens.field_auditor_dashboard.ui.school_activity.SchoolActivityFragment
import dagger.Subcomponent

/**
 * Created by Nitin Chorge on 06-01-2021.
 */

@ActivityScope
@Subcomponent
interface FieldAuditorDashboardComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): FieldAuditorDashboardComponent
    }

    fun inject(activity: FieldAuditorDashboard)
    fun inject(dashboard: DashboardFragment)
    fun inject(attendence: AttendenceFragment)
    fun inject(schoolActivity: SchoolActivityFragment)
    fun inject(mobiliserVisits: MobiliserVisitsFragment)
}