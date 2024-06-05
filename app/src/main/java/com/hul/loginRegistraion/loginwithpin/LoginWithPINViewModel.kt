package com.hul.loginRegistraion.loginwithpin

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.hul.user.UserInfo
import com.hul.utils.initialLetterValidation
import com.hul.utils.numberValidation
import com.hul.utils.pinValidation
import com.hul.utils.repaetValidation
import javax.inject.Inject

/**
 * Created by Nitin Chorge on 06-01-2021.
 */
class LoginWithPINViewModel @Inject constructor(
    private val userInfo: UserInfo,
) : ViewModel() {

    var loginId = MutableLiveData<String>()

    var pin = MutableLiveData<String>()

    val loginEnabled = MediatorLiveData<Boolean>(true)

    val buttonClicked = MutableLiveData<Boolean>(false)

    init {
        loginEnabled.addSource(loginId) {
            loginEnabled.value =
                numberValidation(it) && repaetValidation(it) && initialLetterValidation(it)
        }

//        loginEnabled.addSource(pin) {
//            loginEnabled.value =
//                pinValidation(it) && isLoginIdValid.value == Valid
//        }
    }


    private val isLoginIdValid: LiveData<LoginState> = loginId.map {
        when (numberValidation(it) && repaetValidation(it) && initialLetterValidation(it)) {
            true -> Valid
            false -> InValid
        }
    }

    //loginId Error
    val loginIdError: LiveData<String> = isLoginIdValid.map {
        when (it) {
            Valid -> ""
            InValid -> "Enter correct 10 digit mobile number" // It should be get from R.string
        }
    }

//    private val isPinValid: LiveData<LoginState> = pin.map {
//
//        when (pinValidation(it)) {
//            true -> Valid
//            false -> InValid
//        }
//    }


//    fun calculateLoginButtonState(): LiveData<Boolean> {
//
//        if (isPinValid.value == Valid && isLoginIdValid.value == Valid) {
//            return MutableLiveData(true)
//        } else {
//            return MutableLiveData(false)
//        }
//    }

//    val pinError: LiveData<String> = isPinValid.map {
//        when (it) {
//            Valid -> ""
//            InValid -> "Enter 4 Digit PIN"
//        }
//    }

}