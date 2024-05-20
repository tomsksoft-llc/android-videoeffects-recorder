package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.graphics.BitmapFactory
import android.util.Log
import android.view.Surface
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomsksoft.videoeffectsrecorder.data.AndroidCamera
import com.tomsksoft.videoeffectsrecorder.domain.entity.BackgroundMode
import com.tomsksoft.videoeffectsrecorder.domain.boundary.Camera
import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraManager
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraRecordManager
import com.tomsksoft.videoeffectsrecorder.domain.entity.ColorCorrection
import com.tomsksoft.videoeffectsrecorder.domain.entity.FlashMode
import com.tomsksoft.videoeffectsrecorder.ui.entity.CameraUiState
import com.tomsksoft.videoeffectsrecorder.ui.entity.PrimaryFiltersMode
import com.tomsksoft.videoeffectsrecorder.ui.entity.SecondaryFiltersMode
import com.tomsksoft.videoeffectsrecorder.ui.getNextFlashMode
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val cameraManager: CameraManager
): ViewModel(), CameraViewModel {
    companion object {
        private const val TAG = "Camera View Model"
    }

    private var _cameraConfig: CameraConfig
        get() = cameraManager.cameraConfig.value!!
        set(value) = cameraManager.cameraConfig.onNext(value)

    override val cameraConfig: CameraConfig
        get() = _cameraConfig

    private val _cameraUiState : MutableStateFlow<CameraUiState> = MutableStateFlow(
        CameraUiState(
        flashMode = cameraManager.flashMode,
        primaryFiltersMode =
            if (cameraConfig.colorCorrection != ColorCorrection.NO_FILTER)
                PrimaryFiltersMode.COLOR_CORRECTION
            else
                when(cameraConfig.backgroundMode) {
                    BackgroundMode.Regular ->
                        PrimaryFiltersMode.NONE
                    BackgroundMode.Blur ->
                        PrimaryFiltersMode.BLUR
                    BackgroundMode.Remove, BackgroundMode.Replace ->
                        PrimaryFiltersMode.REPLACE_BACK
            },
        smartZoom = cameraConfig.smartZoom,
        beautification = cameraConfig.beautification,
        isVideoRecording = cameraRecordManager.isRecording,
        isCameraInitialized = true, // TODO [tva] check if EffectsSDK is initialized
        pipelineCameraDirection = cameraManager.direction,
        colorCorrectionMode = cameraConfig.colorCorrection,
        colorCorrectionPower = cameraConfig.colorCorrectionPower
    )
    )
    override val cameraUiState: StateFlow<CameraUiState> = _cameraUiState.asStateFlow()

    override fun setSurface(surface: Surface?) =
        (cameraManager.camera as AndroidCamera).setSurface(surface)

    override fun changeFlashMode() {
        setFlashMode(cameraUiState.value.flashMode.getNextFlashMode())
    }

    private fun setFlashMode(mode: FlashMode) {
        _cameraUiState.update{ cameraUiState ->
            cameraUiState.copy(
                flashMode = mode
            )
        }
        cameraManager.flashMode = mode
    }

    override fun setPrimaryFilter(filtersMode: PrimaryFiltersMode) {
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                primaryFiltersMode = filtersMode
            )
        }
        when (filtersMode) {
            PrimaryFiltersMode.BLUR -> _cameraConfig = _cameraConfig.copy(
                backgroundMode = BackgroundMode.Blur
            )
            PrimaryFiltersMode.REPLACE_BACK -> _cameraConfig = _cameraConfig.copy(
                backgroundMode =
                    if (_cameraConfig.background == null)
                        BackgroundMode.Remove
                    else BackgroundMode.Replace
            )
            PrimaryFiltersMode.COLOR_CORRECTION -> _cameraConfig = _cameraConfig.copy(
                backgroundMode = BackgroundMode.Regular,
                colorCorrection = ColorCorrection.NO_FILTER
            )
            PrimaryFiltersMode.NONE -> _cameraConfig = _cameraConfig.copy(
                backgroundMode = BackgroundMode.Regular,
                colorCorrection = ColorCorrection.NO_FILTER
            )
        }
    }

    override fun setSecondaryFilters(filtersMode: SecondaryFiltersMode) {
        _cameraUiState.update {cameraUiState ->
            when(filtersMode) {
                SecondaryFiltersMode.BEAUTIFY -> {
                    cameraUiState.copy(
                        beautification = if (cameraUiState.beautification == null) 25 else null
                    )
                }

                SecondaryFiltersMode.SMART_ZOOM -> {
                    cameraUiState.copy(
                        smartZoom = if (cameraUiState.smartZoom == null) 25 else null
                    )
                }

                SecondaryFiltersMode.SHARPNESS -> {
                    cameraUiState.copy(
                        sharpnessPower = if (cameraUiState.sharpnessPower == null) 0.25f else null
                    )
                }
            }
        }
        _cameraConfig = when(filtersMode) {
            SecondaryFiltersMode.BEAUTIFY -> _cameraConfig.copy(
                beautification = _cameraConfig.beautification
            )
            SecondaryFiltersMode.SMART_ZOOM -> _cameraConfig.copy(
                smartZoom = _cameraConfig.smartZoom
            )
            SecondaryFiltersMode.SHARPNESS -> _cameraConfig.copy(
                sharpnessPower = _cameraConfig.sharpnessPower
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
                    _cameraConfig = _cameraConfig.copy(
                        background = background,
                        backgroundMode = BackgroundMode.Replace
                    )
                }
        }
    }

    override fun removeBackground() {
        _cameraConfig = _cameraConfig.copy(
            background = null,
            backgroundMode = BackgroundMode.Remove
        )
    }

    override fun flipCamera() {
        if (cameraManager.direction == Camera.Direction.FRONT)
            setFlashMode(FlashMode.OFF)

        cameraManager.direction =
            if (cameraManager.direction == Camera.Direction.BACK)
                Camera.Direction.FRONT
            else
                Camera.Direction.BACK

        _cameraUiState.update {cameraUiState ->
            cameraUiState.copy(
                pipelineCameraDirection = cameraManager.direction
            )
        }
    }

    override fun captureImage() {
        Log.d(TAG, "Capture image")
        cameraRecordManager.takePhoto()
    }

    override fun startVideoRecording() {
        Log.d(TAG, "Start recording")

        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                isVideoRecording = true
            )
        }
        cameraRecordManager.isRecording = true

    }

    override fun stopVideoRecording() {
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                isVideoRecording = false
            )
        }
        Log.d(TAG, "Stop recording")
        cameraRecordManager.isRecording = false
    }

    override fun setBlurPower(value: Float) {
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                blur = value
            )
        }
        _cameraConfig = _cameraConfig.copy(blurPower = value)
    }

    override fun setZoomPower(value: Float) {
        val percent = (value * 100).toInt()
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                smartZoom = percent
            )
        }
        _cameraConfig = _cameraConfig.copy(
            smartZoom = percent
        )
    }

    override fun setBeautifyPower(value: Float) {
        val percent = (value * 100).toInt()
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                beautification = percent
            )
        }
        _cameraConfig = _cameraConfig.copy(beautification = percent)
    }

    override fun setColorCorrectionMode(mode: ColorCorrection, colorGradingSource: InputStream?) {
        Log.d(TAG, "${mode.name} was chosen as color correction mode")
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                colorCorrectionMode = mode
            )
        }
        if (colorGradingSource == null)
            _cameraConfig = _cameraConfig.copy(colorCorrection = mode)
        else viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                colorGradingSource.use(BitmapFactory::decodeStream)
            }
            if (_cameraUiState.value.colorCorrectionMode == ColorCorrection.COLOR_GRADING)
                withContext(Dispatchers.Main) {
                    _cameraConfig = _cameraConfig.copy(
                        colorCorrection = ColorCorrection.COLOR_GRADING,
                        colorGradingSource = bitmap
                    )
                }
        }
    }

    override fun setColorCorrectionPower(value: Float) {
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                colorCorrectionPower = value
            )
        }
        _cameraConfig = _cameraConfig.copy(colorCorrectionPower = value)
    }

    override fun setSharpnessPower(value: Float) {
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                sharpnessPower = value
            )
        }
        _cameraConfig = _cameraConfig.copy(sharpnessPower = value)
    }
}