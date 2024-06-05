package com.hul.curriculam

import com.hul.curriculam.ui.formDetails.FormDetailsFragment
import com.hul.curriculam.ui.formFill.FormFillFragment
import com.hul.curriculam.ui.schoolCode.SchoolCodeFragment
import com.hul.curriculam.ui.schoolForm.SchoolFormFragment
import com.hul.di.ActivityScope
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
    fun inject(fragment: FormDetailsFragment)
    fun inject(fragment: FormFillFragment)

}