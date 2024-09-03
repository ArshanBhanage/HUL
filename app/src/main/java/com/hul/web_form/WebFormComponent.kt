package com.hul.web_form

import com.hul.di.ActivityScope
import com.hul.web_form.dynamicFormDetails.DynamicFormDetailsFragment
import com.hul.web_form.dynamicFormFill.DynamicFormFillFragment
import com.hul.web_form.dynamic_forms.DynamicFormFragment
import dagger.Subcomponent

/**
 * Created by Nitin Chorge on 06-01-2021.
 */

@ActivityScope
@Subcomponent
interface WebFormComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): WebFormComponent
    }

    fun inject(activity: WebForm)
    fun inject(fragment : DynamicFormFragment)
    fun inject(fragment : DynamicFormFillFragment)
    fun inject(fragment : DynamicFormDetailsFragment)

}