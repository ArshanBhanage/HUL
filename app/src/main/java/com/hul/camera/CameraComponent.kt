package com.hul.camera

import com.hul.camera.cameraPreview.CameraPreviewFragment
import com.hul.camera.imagePreview.ImagePreviewFragment
import com.hul.di.ActivityScope
import dagger.Subcomponent

/**
 * Created by Nitin Chorge on 06-01-2021.
 */

@ActivityScope
@Subcomponent
interface CameraComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): CameraComponent
    }

    fun inject(activity: CameraActivity)
    fun inject(fragment: CameraPreviewFragment)
    fun inject(fragment: ImagePreviewFragment)

}