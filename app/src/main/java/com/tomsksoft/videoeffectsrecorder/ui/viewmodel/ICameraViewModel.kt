package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.view.Surface
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.ColorCorrection
import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream

interface ICameraViewModel {
    val cameraUiState: StateFlow<CameraUiState>
    val cameraConfigData: CameraConfig

    fun setSurface(surface: Surface?)
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

    /**
     * @param colorGradingSource binary representation of bitmap, required only
     * for {@link com.tomsksoft.videoeffectsrecorder.domain.ColorCorrection#COLOR_GRADING} mode,
     * MUST be null if different mode is chosen
     */
    fun setColorCorrectionMode(mode: ColorCorrection, colorGradingSource: InputStream? = null)
}