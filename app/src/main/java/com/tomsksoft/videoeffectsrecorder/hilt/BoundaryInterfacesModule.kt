package com.tomsksoft.videoeffectsrecorder.hilt

import android.content.Context
import com.tomsksoft.videoeffectsrecorder.BuildConfig
import com.tomsksoft.videoeffectsrecorder.data.CameraImpl
import com.tomsksoft.videoeffectsrecorder.data.FrameProcessorImpl
import com.tomsksoft.videoeffectsrecorder.data.PhotoPickerImpl
import com.tomsksoft.videoeffectsrecorder.data.VideoRecorderImpl
import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.FrameProcessor
import com.tomsksoft.videoeffectsrecorder.domain.PhotoPicker
import com.tomsksoft.videoeffectsrecorder.domain.VideoRecorder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BoundaryInterfacesModule {
    @Provides
    @Singleton
    fun provideCamera(@ApplicationContext context: Context): Camera =
        CameraImpl(context, Camera.Direction.BACK).apply {
            isEnabled = true
        }

    @Provides
    @Singleton
    fun provideVideoRecorder(@ApplicationContext context: Context): VideoRecorder =
        VideoRecorderImpl(context, BuildConfig.RECORDS_DIRECTORY)

    @Provides
    @Singleton
    fun provideFrameProcessor(@ApplicationContext context: Context): FrameProcessor =
        FrameProcessorImpl(context)

    @Provides
    @Singleton
    fun providePhotoPicker(@ApplicationContext context: Context): PhotoPicker =
        PhotoPickerImpl(context, BuildConfig.RECORDS_DIRECTORY)
}