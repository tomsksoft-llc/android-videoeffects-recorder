package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.graphics.Bitmap
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream

interface ICameraViewModel {
    val cameraConfig: StateFlow<CameraConfig>
    val cameraUiState: StateFlow<CameraUiState>
    val frame: Observable<Bitmap>

    fun setFlash(flashMode: FlashMode)
    fun setPrimaryFilter(filtersMode: PrimaryFiltersMode)
    fun setSecondaryFilters(filtersMode: SecondaryFiltersMode)
    fun setBackground(bitmapStream: InputStream)
    fun removeBackground()
    fun toggleQuickSettingsIndicator(expandedTopBarMode: ExpandedTopBarMode)
    fun flipCamera()
    fun captureImage()
    fun startVideoRecording()
    fun stopVideoRecording()
    fun setBlurPower(value: Float)
    fun setZoomPower(value: Float)
    fun setBeautifyPower(value: Float)
    fun setColorCorrectionMode(mode: CameraConfig.ColorCorrection)
}