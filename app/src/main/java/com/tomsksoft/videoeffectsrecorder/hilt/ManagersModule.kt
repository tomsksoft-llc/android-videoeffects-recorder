package com.tomsksoft.videoeffectsrecorder.hilt

import android.content.Context
import com.tomsksoft.videoeffectsrecorder.BuildConfig
import com.tomsksoft.videoeffectsrecorder.data.CameraImpl
import com.tomsksoft.videoeffectsrecorder.data.VideoRecorderImpl
import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.CameraRecordManager
import com.tomsksoft.videoeffectsrecorder.domain.VideoRecorder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ManagersModule {
    @Provides
    @Singleton
    fun provideCamera(@ApplicationContext context: Context): Camera =
        CameraImpl(context, Camera.Direction.BACK)

    @Provides
    @Singleton
    fun provideVideoRecorder(@ApplicationContext context: Context): VideoRecorder =
        VideoRecorderImpl(context, BuildConfig.RECORDS_DIRECTORY)

    @Provides
    @Singleton
    fun provideRecordManager(camera: Camera, videoRecorder: VideoRecorder) =
        CameraRecordManager(camera, videoRecorder)
}