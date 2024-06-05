package com.hul.camera.cameraPreview

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hul.user.UserInfo
import javax.inject.Inject

class CameraPreviewViewModel @Inject constructor(
    private val userInfo: UserInfo,
) : ViewModel() {

    var uri = MutableLiveData<Uri>()

    var longitude = MutableLiveData<String>()
    var lattitude = MutableLiveData<String>()
}