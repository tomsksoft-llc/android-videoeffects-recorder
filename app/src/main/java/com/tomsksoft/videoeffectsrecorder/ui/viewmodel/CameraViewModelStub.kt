package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.view.Surface
import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.entity.ColorCorrection
import com.tomsksoft.videoeffectsrecorder.ui.entity.CameraUiState
import com.tomsksoft.videoeffectsrecorder.ui.entity.PrimaryFiltersMode
import com.tomsksoft.videoeffectsrecorder.ui.entity.SecondaryFiltersMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream

object CameraViewModelStub: CameraViewModel {
    override val cameraUiState: StateFlow<CameraUiState> = MutableStateFlow(CameraUiState())
    override val cameraConfigData: CameraConfig = CameraConfig()

    override fun setSurface(surface: Surface?) = throw unimplementedError()
    override fun setFlash() = throw unimplementedError()
    override fun setPrimaryFilter(filtersMode: PrimaryFiltersMode) = throw unimplementedError()
    override fun setSecondaryFilters(filtersMode: SecondaryFiltersMode) = throw unimplementedError()
    override fun setBackground(bitmapStream: InputStream) = throw unimplementedError()
    override fun removeBackground() = throw unimplementedError()
    override fun flipCamera() = throw unimplementedError()
    override fun captureImage() = throw unimplementedError()
    override fun startVideoRecording() = throw unimplementedError()
    override fun stopVideoRecording() = throw unimplementedError()
    override fun setBlurPower(value: Float) = throw unimplementedError()
    override fun setZoomPower(value: Float) = throw unimplementedError()
    override fun setBeautifyPower(value: Float) = throw unimplementedError()
    override fun setColorCorrectionMode(mode: ColorCorrection, colorGradingSource: InputStream?) = throw unimplementedError()
    override fun setColorCorrectionPower(value: Float) = throw unimplementedError()
    override fun setSharpnessPower(value: Float) = throw unimplementedError()

    private fun unimplementedError() = NotImplementedError("Stub doesn't implement any logic")
}