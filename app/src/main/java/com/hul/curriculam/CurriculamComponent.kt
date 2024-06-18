package com.hul.curriculam

import com.hul.curriculam.ui.form1Details.Form1DetailsFragment
import com.hul.curriculam.ui.form1Fill.Form1FillFragment
import com.hul.curriculam.ui.form2Details.Form2DetailsFragment
import com.hul.curriculam.ui.form2Fill.Form2FillFragment
import com.hul.curriculam.ui.form3Fill.Form3FillFragment
import com.hul.curriculam.ui.schoolCode.SchoolCodeFragment
import com.hul.curriculam.ui.schoolForm.SchoolFormFragment
import com.hul.di.ActivityScope
import com.hul.screens.field_auditor_dashboard.ui.school_activity.AuditorSchoolFormFragment
import com.hul.screens.field_auditor_dashboard.ui.school_activity.form1Details.AuditorForm1DetailsFragment
import com.hul.screens.field_auditor_dashboard.ui.school_activity.form1Fill.AuditorForm1FillFragment
import com.hul.screens.field_auditor_dashboard.ui.school_activity.form2Details.AuditorForm2DetailsFragment
import com.hul.screens.field_auditor_dashboard.ui.school_activity.form2Fill.AuditorForm2FillFragment
import com.hul.screens.field_auditor_dashboard.ui.school_activity.form3Fill.AuditorForm3FillFragment
import dagger.Subcomponent

/**
 * Created by Nitin Chorge on 06-01-2021.
 */

@ActivityScope
@Subcomponent
interface CurriculamComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): CurriculamComponent
    }

    fun inject(activity: Curriculam)
    fun inject(fragment: SchoolCodeFragment)
    fun inject(fragment: SchoolFormFragment)
    fun inject(fragment: Form1DetailsFragment)
    fun inject(fragment: Form2DetailsFragment)
    fun inject(fragment: Form1FillFragment)
    fun inject(fragment: Form2FillFragment)
    fun inject(fragment: Form3FillFragment)

    fun inject(fragment: AuditorForm1DetailsFragment)
    fun inject(fragment: AuditorForm2DetailsFragment)
    fun inject(fragment: AuditorForm1FillFragment)
    fun inject(fragment: AuditorForm2FillFragment)
    fun inject(fragment: AuditorForm3FillFragment)
    fun inject(fragment: AuditorSchoolFormFragment)

}