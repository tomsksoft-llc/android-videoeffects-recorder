package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.graphics.Bitmap
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.InputStream

object CameraViewModelStub: ICameraViewModel {
    override val cameraUiState: StateFlow<CameraUiState> = MutableStateFlow(CameraUiState(
        flashMode = FlashMode.AUTO,
        expandedTopBarMode = ExpandedTopBarMode.DEFAULT,
        primaryFiltersMode = PrimaryFiltersMode.NONE,
        isSmartZoomEnabled = false,
        isBeautifyEnabled = false,
        isVideoRecording = false,
        isCameraInitialized = true,
    ))
    override val frame: Observable<Bitmap> = BehaviorSubject.create()
    override val cameraConfigData: CameraConfig = CameraConfig()

    override fun setFlash(flashMode: FlashMode) = throw unimplementedError()
    override fun setPrimaryFilter(filtersMode: PrimaryFiltersMode) = throw unimplementedError()
    override fun setSecondaryFilters(filtersMode: SecondaryFiltersMode) = throw unimplementedError()
    override fun setBackground(bitmapStream: InputStream) = throw unimplementedError()
    override fun removeBackground() = throw unimplementedError()
    override fun toggleQuickSettingsIndicator(expandedTopBarMode: ExpandedTopBarMode) = throw unimplementedError()
    override fun flipCamera() = throw unimplementedError()
    override fun captureImage() = throw unimplementedError()
    override fun startVideoRecording() = throw unimplementedError()
    override fun stopVideoRecording() = throw unimplementedError()
    override fun setBlurPower(value: Float) = throw unimplementedError()
    override fun setZoomPower(value: Float) = throw unimplementedError()
    override fun setBeautifyPower(value: Float) = throw unimplementedError()
    override fun setColorCorrectionMode(mode: CameraConfig.ColorCorrection) = throw unimplementedError()

    private fun unimplementedError() = NotImplementedError("Stub doesn't implement any logic")
}