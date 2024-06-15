package com.hul.curriculam

import com.hul.curriculam.ui.form1Details.Form1DetailsFragment
import com.hul.curriculam.ui.form1Fill.Form1FillFragment
import com.hul.curriculam.ui.form2Details.Form2DetailsFragment
import com.hul.curriculam.ui.form2Fill.Form2FillFragment
import com.hul.curriculam.ui.form3Fill.Form3FillFragment
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
    fun inject(fragment: Form1DetailsFragment)
    fun inject(fragment: Form2DetailsFragment)
    fun inject(fragment: Form1FillFragment)
    fun inject(fragment: Form2FillFragment)
    fun inject(fragment: Form3FillFragment)

}