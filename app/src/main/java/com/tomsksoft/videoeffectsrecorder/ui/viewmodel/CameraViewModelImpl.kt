package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tomsksoft.videoeffectsrecorder.data.FrameMapper
import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.CameraManager
import com.tomsksoft.videoeffectsrecorder.domain.CameraRecordManager
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import javax.inject.Inject

@HiltViewModel
class CameraViewModelImpl @Inject constructor(
    private val cameraRecordManager: CameraRecordManager,
    private val cameraManager: CameraManager,
    app: Application
): AndroidViewModel(app), ICameraViewModel {
    companion object {
        private const val TAG = "Camera View Model"
    }

    private var cameraConfig: CameraConfig
        get() = cameraManager.cameraConfig.value!!
        set(value) = cameraManager.cameraConfig.onNext(value)

    override val cameraConfigData: CameraConfig
        get() = cameraConfig

    private val _cameraUiState : MutableStateFlow<CameraUiState>
            = MutableStateFlow(CameraUiState(
        flashMode = FlashMode.AUTO,
        expandedTopBarMode = ExpandedTopBarMode.DEFAULT,
        primaryFiltersMode = PrimaryFiltersMode.NONE,
        isSmartZoomEnabled = cameraConfig.smartZoom != null,
        isBeautifyEnabled = cameraConfig.beautification != null,
        isVideoRecording = cameraRecordManager.isRecording,
        isCameraInitialized = true // TODO [tva] check if EffectsSDK is initialized
    ))
    override val cameraUiState: StateFlow<CameraUiState> = _cameraUiState.asStateFlow()

    override val frame: Observable<Bitmap> = cameraManager.frameSource
        .map(FrameMapper::fromAny)
        .observeOn(AndroidSchedulers.mainThread())

    override fun setFlash(flashMode: FlashMode) {
        _cameraUiState.update{cameraUiState ->
            cameraUiState.copy(
                flashMode = flashMode
            )
        }
        // TODO [fmv]: add usecase interaction
    }

    override fun setPrimaryFilter(filtersMode: PrimaryFiltersMode) {
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                primaryFiltersMode = filtersMode
            )
        }
        when (filtersMode) {
            PrimaryFiltersMode.BLUR -> cameraConfig = cameraConfig.copy(
                backgroundMode = CameraConfig.BackgroundMode.Blur,
                blurPower = 0f
            )
            PrimaryFiltersMode.REPLACE_BACK -> cameraConfig = cameraConfig.copy(
                backgroundMode =
                    if (cameraConfig.background == null)
                        CameraConfig.BackgroundMode.Remove
                    else CameraConfig.BackgroundMode.Replace
            )
            PrimaryFiltersMode.COLOR_CORRECTION -> cameraConfig = cameraConfig.copy(
                backgroundMode = CameraConfig.BackgroundMode.Regular,
                colorCorrection = CameraConfig.ColorCorrection.COLOR_GRADING
            )
            PrimaryFiltersMode.NONE -> cameraConfig = cameraConfig.copy(
                backgroundMode = CameraConfig.BackgroundMode.Regular,
                colorCorrection = CameraConfig.ColorCorrection.NO_FILTER
            )
        }
    }

    override fun setSecondaryFilters(filtersMode: SecondaryFiltersMode) {
        _cameraUiState.update {cameraUiState ->
            when(filtersMode) {
                SecondaryFiltersMode.BEAUTIFY -> {
                    cameraUiState.copy(
                        isBeautifyEnabled = !cameraUiState.isBeautifyEnabled
                    )
                }

                SecondaryFiltersMode.SMART_ZOOM -> {
                    cameraUiState.copy(
                        isSmartZoomEnabled = !cameraUiState.isSmartZoomEnabled
                    )
                }
            }
        }
        when(filtersMode) {
            SecondaryFiltersMode.BEAUTIFY -> cameraConfig = cameraConfig.copy(
                beautification =
                    if (!cameraUiState.value.isBeautifyEnabled) 0
                    else cameraConfig.beautification
            )
            SecondaryFiltersMode.SMART_ZOOM -> cameraConfig = cameraConfig.copy(
                smartZoom =
                    if (!cameraUiState.value.isSmartZoomEnabled) 0
                    else cameraConfig.smartZoom
            )
        }
    }

    override fun setBackground(bitmapStream: InputStream) {
        viewModelScope.launch {
            val background = withContext(Dispatchers.IO) {
                bitmapStream.use(BitmapFactory::decodeStream)
            }
            if (_cameraUiState.value.primaryFiltersMode == PrimaryFiltersMode.REPLACE_BACK)
                withContext(Dispatchers.Main) {
                    cameraConfig = cameraConfig.copy(
                        background = background,
                        backgroundMode = CameraConfig.BackgroundMode.Replace
                    )
                }
        }
    }

    override fun removeBackground() {
        cameraConfig = cameraConfig.copy(
            background = null,
            backgroundMode = CameraConfig.BackgroundMode.Remove
        )
    }

    override fun toggleQuickSettingsIndicator(expandedTopBarMode: ExpandedTopBarMode) {
        _cameraUiState.update {cameraUiState ->
            cameraUiState.copy(
                expandedTopBarMode = expandedTopBarMode
            )
        }
    }

    override fun flipCamera() {
        cameraManager.direction =
            if (cameraManager.direction == Camera.Direction.BACK)
                Camera.Direction.FRONT
            else
                Camera.Direction.BACK
    }

    override fun captureImage() {
        Log.d(TAG, "Capture image")
        // TODO: [fmv] add usecase interaction
    }

    override fun startVideoRecording() {
        Log.d(TAG, "Start recording")

        _cameraUiState.update {cameraUiState ->
            cameraUiState.copy(
                isVideoRecording = true
            )
        }
        cameraRecordManager.isRecording = true

    }

    override fun stopVideoRecording() {
        _cameraUiState.update {cameraUiState ->
            cameraUiState.copy(
                isVideoRecording = false
            )
        }
        Log.d(TAG, "Stop recording")
        cameraRecordManager.isRecording = false
    }

    override fun setBlurPower(value: Float) {
        cameraConfig = cameraConfig.copy(blurPower = value)
    }

    override fun setZoomPower(value: Float) {
        cameraConfig = cameraConfig.copy(
            smartZoom = (value*100).toInt()
        )
    }

    override fun setBeautifyPower(value: Float) {
        cameraConfig = cameraConfig.copy(beautification = (value*100).toInt())
    }

    override fun setColorCorrectionMode(mode: CameraConfig.ColorCorrection) {
        Log.d(TAG, "${mode.name} was chosen as color correction mode")
        cameraConfig = cameraConfig.copy(colorCorrection = mode)
    }
}