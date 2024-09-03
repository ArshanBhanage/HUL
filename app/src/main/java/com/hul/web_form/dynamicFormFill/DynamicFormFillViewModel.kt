package com.hul.web_form.dynamicFormFill

import androidx.lifecycle.ViewModel
import com.hul.user.UserInfo
import javax.inject.Inject

class DynamicFormFillViewModel  @Inject constructor(
    private val userInfo: UserInfo,
)  : ViewModel()  {
}