package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.app.Application
import android.graphics.BitmapFactory
import android.util.Log
import android.view.Surface
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tomsksoft.videoeffectsrecorder.domain.BackgroundMode
import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.CameraManager
import com.tomsksoft.videoeffectsrecorder.domain.CameraRecordManager
import com.tomsksoft.videoeffectsrecorder.domain.ColorCorrection
import com.tomsksoft.videoeffectsrecorder.domain.FlashMode
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
        flashMode = FlashMode.OFF,
        expandedTopBarMode = ExpandedTopBarMode.DEFAULT,
        primaryFiltersMode = PrimaryFiltersMode.NONE,
        smartZoom = cameraConfig.smartZoom,
        beautification = cameraConfig.beautification,
        isVideoRecording = cameraRecordManager.isRecording,
        isCameraInitialized = true, // TODO [tva] check if EffectsSDK is initialized
        pipelineCameraDirection = Camera.Direction.BACK,
        colorCorrectionMode = ColorCorrection.NO_FILTER,
        colorCorrectionPower = cameraConfig.colorCorrectionPower
    ))
    override val cameraUiState: StateFlow<CameraUiState> = _cameraUiState.asStateFlow()

    override fun setSurface(surface: Surface?) = cameraManager.setSurface(surface)

    override fun setFlash(flashMode: FlashMode) {
        _cameraUiState.update{cameraUiState ->
            cameraUiState.copy(
                flashMode = flashMode
            )
        }
        Log.d("Flash", "mode: ${flashMode}")
        cameraManager.flashMode = flashMode
    }

    override fun setPrimaryFilter(filtersMode: PrimaryFiltersMode) {
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                primaryFiltersMode = filtersMode
            )
        }
        when (filtersMode) {
            PrimaryFiltersMode.BLUR -> cameraConfig = cameraConfig.copy(
                backgroundMode = BackgroundMode.Blur
            )
            PrimaryFiltersMode.REPLACE_BACK -> cameraConfig = cameraConfig.copy(
                backgroundMode =
                    if (cameraConfig.background == null)
                        BackgroundMode.Remove
                    else BackgroundMode.Replace
            )
            PrimaryFiltersMode.COLOR_CORRECTION -> cameraConfig = cameraConfig.copy(
                backgroundMode = BackgroundMode.Regular,
                colorCorrection = ColorCorrection.NO_FILTER
            )
            PrimaryFiltersMode.NONE -> cameraConfig = cameraConfig.copy(
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
        cameraConfig = when(filtersMode) {
            SecondaryFiltersMode.BEAUTIFY -> cameraConfig.copy(
                beautification = cameraConfig.beautification
            )
            SecondaryFiltersMode.SMART_ZOOM -> cameraConfig.copy(
                smartZoom = cameraConfig.smartZoom
            )
            SecondaryFiltersMode.SHARPNESS -> cameraConfig.copy(
                sharpnessPower = cameraConfig.sharpnessPower
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
                        backgroundMode = BackgroundMode.Replace
                    )
                }
        }
    }

    override fun removeBackground() {
        cameraConfig = cameraConfig.copy(
            background = null,
            backgroundMode = BackgroundMode.Remove
        )
    }

    override fun toggleQuickSettingsIndicator(expandedTopBarMode: ExpandedTopBarMode) {
        _cameraUiState.update { cameraUiState ->
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
        cameraConfig = cameraConfig.copy(blurPower = value)
    }

    override fun setZoomPower(value: Float) {
        val percent = (value * 100).toInt()
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                smartZoom = percent
            )
        }
        cameraConfig = cameraConfig.copy(
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
        cameraConfig = cameraConfig.copy(beautification = percent)
    }

    override fun setColorCorrectionMode(mode: ColorCorrection, colorGradingSource: InputStream?) {
        Log.d(TAG, "${mode.name} was chosen as color correction mode")
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                colorCorrectionMode = mode
            )
        }
        if (colorGradingSource == null)
            cameraConfig = cameraConfig.copy(colorCorrection = mode)
        else viewModelScope.launch {
            val bitmap = withContext(Dispatchers.IO) {
                colorGradingSource.use(BitmapFactory::decodeStream)
            }
            if (_cameraUiState.value.colorCorrectionMode == ColorCorrection.COLOR_GRADING)
                withContext(Dispatchers.Main) {
                    cameraConfig = cameraConfig.copy(
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
        cameraConfig = cameraConfig.copy(colorCorrectionPower = value)
    }

    override fun setSharpnessPower(value: Float) {
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                sharpnessPower = value
            )
        }
        cameraConfig = cameraConfig.copy(sharpnessPower = value)
    }
}