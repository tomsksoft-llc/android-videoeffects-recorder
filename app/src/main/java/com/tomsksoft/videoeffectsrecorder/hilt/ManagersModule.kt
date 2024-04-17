package com.tomsksoft.videoeffectsrecorder.hilt

import com.tomsksoft.videoeffectsrecorder.domain.CameraManager
import com.tomsksoft.videoeffectsrecorder.domain.CameraRecordManager
import com.tomsksoft.videoeffectsrecorder.domain.EffectsPipelineCamera
import com.tomsksoft.videoeffectsrecorder.domain.PhotoPicker
import com.tomsksoft.videoeffectsrecorder.domain.VideoRecorder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ManagersModule {
    @Provides
    @Singleton
    fun provideCameraManager(effectsPipelineCamera: EffectsPipelineCamera) =
        CameraManager(effectsPipelineCamera)

    @Provides
    @Singleton
    fun provideRecordManager(
        cameraManager: CameraManager,
        videoRecorder: VideoRecorder,
        photoPicker: PhotoPicker
    ) = CameraRecordManager(cameraManager, videoRecorder, photoPicker)
}