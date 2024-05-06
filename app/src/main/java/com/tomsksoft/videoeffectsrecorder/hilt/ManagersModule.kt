package com.tomsksoft.videoeffectsrecorder.hilt

import com.tomsksoft.videoeffectsrecorder.domain.boundary.Camera
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraManager
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraRecordManager
import com.tomsksoft.videoeffectsrecorder.domain.boundary.PhotoPicker
import com.tomsksoft.videoeffectsrecorder.domain.boundary.VideoRecorder
import com.tomsksoft.videoeffectsrecorder.domain.usecase.GalleryManager
import com.tomsksoft.videoeffectsrecorder.domain.boundary.MediaPicker
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
    fun provideCameraManager(camera: Camera) =
        CameraManager(camera)

    @Provides
    @Singleton
    fun provideRecordManager(
        cameraManager: CameraManager,
        videoRecorder: VideoRecorder,
        photoPicker: PhotoPicker
    ) = CameraRecordManager(cameraManager, videoRecorder, photoPicker)

    @Provides
    @Singleton
    fun provideGalleryManager(
        mediaPicker: MediaPicker
    ) = GalleryManager(mediaPicker)
}