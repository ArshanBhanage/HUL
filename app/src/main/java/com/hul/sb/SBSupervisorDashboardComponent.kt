package com.hul.sb

import com.hul.di.ActivityScope
import com.hul.sb.mobiliser.SBMobiliserDashboard
import com.hul.sb.supervisor.ui.dashboard.SBSupervisorDashboardFragment
import com.hul.sb.supervisor.ui.visits.SupervisorVisitsFragment
import dagger.Subcomponent

/**
 * Created by Nitin Chorge on 02-09-2024.
 */
@ActivityScope
@Subcomponent
interface SBSupervisorDashboardComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): SBSupervisorDashboardComponent
    }

    fun inject(activity: SBSupervisorDashboardFragment)

    fun inject(activity: SupervisorVisitsFragment)
}