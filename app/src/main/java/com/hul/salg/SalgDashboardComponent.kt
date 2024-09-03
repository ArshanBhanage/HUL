package com.hul.salg

import com.hul.dashboard.ui.attendence.AttendenceFragment
import com.hul.dashboard.ui.dashboard.DashboardFragment
import com.hul.di.ActivityScope
import com.hul.salg.ui.attendence.SalgAttendenceFragment
import com.hul.salg.ui.dashboard.SalgDashboardFragment
import com.hul.salg.ui.dashboard.SalgDashboardFragmentInterface
import com.hul.salg.ui.formDetails.SalgFormDetailFragment
import com.hul.salg.ui.formFill.SalgFormFillFragment
import com.hul.salg.ui.salgForm.SalgFormFragment
import com.hul.salg.ui.salgPreForm.SalgPreFormFragment
import dagger.Subcomponent

/**
 * Created by Nitin Chorge on 06-01-2021.
 */

@ActivityScope
@Subcomponent
interface SalgDashboardComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): SalgDashboardComponent
    }

    fun inject(activity: SalgDashboard)

    fun inject(fragment: SalgDashboardFragment)

    fun inject(fragment: SalgAttendenceFragment)

    fun inject(fragment: SalgFormFragment)

    fun inject(fragment: SalgFormFillFragment)

    fun inject(fragment: SalgPreFormFragment)

    fun inject(fragment: SalgFormDetailFragment)
}