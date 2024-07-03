package com.hul.web_form

import com.hul.di.ActivityScope
import com.hul.loginRegistraion.loginwithpin.LoginWithPIN
import com.hul.loginRegistraion.otp.OTPFragment
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

}