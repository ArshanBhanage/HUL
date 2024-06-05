package com.hul.dashboard

import com.hul.dashboard.ui.attendence.AttendenceFragment
import com.hul.dashboard.ui.dashboard.DashboardFragment
import com.hul.di.ActivityScope
import dagger.Subcomponent

/**
 * Created by Nitin Chorge on 06-01-2021.
 */

@ActivityScope
@Subcomponent
interface DashboardComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): DashboardComponent
    }

    fun inject(activity: Dashboard)
    fun inject(dashboard: DashboardFragment)
    fun inject(attendence: AttendenceFragment)

}