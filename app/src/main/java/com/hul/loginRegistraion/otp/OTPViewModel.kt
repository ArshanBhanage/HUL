package com.hul.loginRegistraion.otp

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.hul.loginRegistraion.loginwithpin.InValid
import com.hul.loginRegistraion.loginwithpin.LoginState
import com.hul.loginRegistraion.loginwithpin.Valid
import com.hul.user.UserInfo
import com.hul.utils.initialLetterValidation
import com.hul.utils.numberValidation
import com.hul.utils.pinValidation
import com.hul.utils.repaetValidation
import javax.inject.Inject

class OTPViewModel @Inject constructor(
    private val userInfo: UserInfo,
) : ViewModel() {

    var loginId = MutableLiveData<String>()
    var encodedLoginId = MutableLiveData<String>()
    var otp = MutableLiveData<String>()

    var termsAccepted = MutableLiveData<Boolean>(true)

    val loginEnabled: LiveData<Boolean> = otp.map {
        when (pinValidation(it)) {
            true -> true
            false -> false
        }
    }

}