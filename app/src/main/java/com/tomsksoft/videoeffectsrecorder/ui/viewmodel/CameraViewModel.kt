package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.view.Surface
import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.entity.ColorCorrection
import com.tomsksoft.videoeffectsrecorder.ui.entity.CameraUiState
import com.tomsksoft.videoeffectsrecorder.ui.entity.PrimaryFiltersMode
import com.tomsksoft.videoeffectsrecorder.ui.entity.SecondaryFiltersMode
import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream

interface CameraViewModel {
    val cameraUiState: StateFlow<CameraUiState>
    val cameraConfig: CameraConfig

    fun setSurface(surface: Surface?)
    fun changeFlashMode()
    fun setPrimaryFilter(filtersMode: PrimaryFiltersMode)
    fun setSecondaryFilters(filtersMode: SecondaryFiltersMode)
    fun setBackground(bitmapStream: InputStream)
    fun removeBackground()
    fun flipCamera()
    fun captureImage()
    fun startVideoRecording()
    fun stopVideoRecording()
    fun setBlurPower(value: Float)
    fun setZoomPower(value: Float)
    fun setBeautifyPower(value: Float)

    /**
     * @param colorGradingSource binary representation of bitmap, required only
     * for {@link com.tomsksoft.videoeffectsrecorder.domain.entity.ColorCorrection#COLOR_GRADING} mode,
     * MUST be null if different mode is chosen
     */
    fun setColorCorrectionMode(mode: ColorCorrection, colorGradingSource: InputStream? = null)
    fun setColorCorrectionPower(value: Float)
    fun setSharpnessPower(value: Float)
}