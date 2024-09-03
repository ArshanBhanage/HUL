package com.hul.sb

import com.hul.di.ActivityScope
import com.hul.sb.mobiliser.SBMobiliserDashboard
import com.hul.sb.mobiliser.ui.dashboard.SBDashboardMobiliserFragment
import com.hul.sb.mobiliser.ui.sbform1details.SBForm1DetailsFragment
import com.hul.sb.mobiliser.ui.sbform1fill.SBForm1FillFragment
import com.hul.sb.mobiliser.ui.sbform2details.SBForm2DetailsFragment
import com.hul.sb.mobiliser.ui.sbform2fill.SBForm2FillFragment
import com.hul.sb.mobiliser.ui.sbform3details.SBForm3DetailsFragment
import com.hul.sb.mobiliser.ui.sbform3fill.SBForm3FillFragment
import com.hul.sb.mobiliser.ui.sbpreform.SBPreFormFragment
import dagger.Subcomponent

/**
 * Created by Nitin Chorge on 06-01-2021.
 */

@ActivityScope
@Subcomponent
interface SBDashboardComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): SBDashboardComponent
    }

    fun inject(activity: SBMobiliserDashboard)

    fun inject(fragment: SBDashboardMobiliserFragment)

    fun inject(fragment: SBForm1FillFragment)

    fun inject(fragment: SBPreFormFragment)

    fun inject(fragment: SBForm2FillFragment)

    fun inject(fragment: SBForm3FillFragment)

    fun inject(fragment: SBForm1DetailsFragment)

    fun inject(fragment: SBForm2DetailsFragment)

    fun inject(fragment: SBForm3DetailsFragment)
}