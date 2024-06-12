package com.hul.camera.imagePreview

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hul.data.GetVisitDataResponseData
import com.hul.user.UserInfo
import javax.inject.Inject

class ImagePreviewViewModel @Inject constructor(
    private val userInfo: UserInfo,
) : ViewModel() {

    var uri = MutableLiveData<Uri>()

    var longitude = MutableLiveData<String>()
    var lattitude = MutableLiveData<String>()

    var visitData = MutableLiveData<GetVisitDataResponseData>(null)

}