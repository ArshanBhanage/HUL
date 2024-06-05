package com.hul.loginRegistraion

import com.hul.di.ActivityScope
import com.hul.loginRegistraion.loginwithpin.LoginWithPIN
import com.hul.loginRegistraion.otp.OTPFragment
import dagger.Subcomponent

/**
 * Created by Nitin Chorge on 06-01-2021.
 */

@ActivityScope
@Subcomponent
interface LoginRegisterComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): LoginRegisterComponent
    }

    fun inject(activity: LoginRegistrationActivity)
    fun inject(fragment: LoginWithPIN)
    fun inject(fragment: OTPFragment)

}