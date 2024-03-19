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

    private val _cameraUiState : MutableStateFlow<CameraUiState>
            = MutableStateFlow(CameraUiState(
        flashMode = FlashMode.AUTO,
        expandedTopBarMode = ExpandedTopBarMode.DEFAULT,
        primaryFiltersMode = PrimaryFiltersMode.NONE,
        isSmartZoomEnabled = cameraConfig.smartZoom != null,
        isBeautifyEnabled = cameraConfig.beautification != null,
        isVideoRecording = cameraRecordManager.isRecording,
        isCameraInitialized = true, // TODO [tva] check if EffectsSDK is initialized
        currentCameraConfig = cameraConfig // should it store duplicate?
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
                primaryFiltersMode = filtersMode,
                currentCameraConfig =
                    when (filtersMode) {
                        PrimaryFiltersMode.BLUR -> cameraUiState.currentCameraConfig.copy(
                            backgroundMode = CameraConfig.BackgroundMode.Blur,
                            blurPower = 0.5f
                        )
                        PrimaryFiltersMode.REPLACE_BACK -> cameraUiState.currentCameraConfig.copy(
                            backgroundMode =
                                if (cameraUiState.currentCameraConfig.background == null)
                                    CameraConfig.BackgroundMode.Remove
                                else CameraConfig.BackgroundMode.Replace
                        )
                        PrimaryFiltersMode.COLOR_CORRECTION -> cameraUiState.currentCameraConfig.copy(
                            backgroundMode = CameraConfig.BackgroundMode.Regular,
                            colorCorrection = CameraConfig.ColorCorrection.COLOR_GRADING
                        )
                        PrimaryFiltersMode.NONE -> cameraUiState.currentCameraConfig.copy(
                            backgroundMode = CameraConfig.BackgroundMode.Regular,
                            colorCorrection = CameraConfig.ColorCorrection.NO_FILTER
                        )
                    }
            )
        }

        cameraConfig = cameraUiState.value.currentCameraConfig
    }

    override fun setSecondaryFilters(filtersMode: SecondaryFiltersMode) {
        _cameraUiState.update {cameraUiState ->
            when(filtersMode) {
                SecondaryFiltersMode.BEAUTIFY -> {
                    cameraUiState.copy(
                        isBeautifyEnabled = !cameraUiState.isBeautifyEnabled,
                        currentCameraConfig = cameraUiState.currentCameraConfig.copy(
                            beautification =
                                if (cameraUiState.isBeautifyEnabled) 0
                                else cameraUiState.currentCameraConfig.beautification
                        )
                    )
                }

                SecondaryFiltersMode.SMART_ZOOM -> {
                    cameraUiState.copy(
                        isSmartZoomEnabled = !cameraUiState.isSmartZoomEnabled,
                        currentCameraConfig = cameraUiState.currentCameraConfig.copy(
                            smartZoom =
                                if (cameraUiState.isSmartZoomEnabled) 0
                                else cameraUiState.currentCameraConfig.smartZoom
                            )
                    )
                }
            }
        }

        cameraConfig = cameraUiState.value.currentCameraConfig
    }

    override fun setBackground(bitmapStream: InputStream) {
        viewModelScope.launch {
            val background = withContext(Dispatchers.IO) {
                bitmapStream.use(BitmapFactory::decodeStream)
            }
            _cameraUiState.update {cameraUiState ->
                cameraUiState.copy(
                    currentCameraConfig = cameraUiState.currentCameraConfig.copy(
                        background = background,
                        backgroundMode = CameraConfig.BackgroundMode.Replace
                    )
                )
            }
            if (_cameraUiState.value.primaryFiltersMode == PrimaryFiltersMode.REPLACE_BACK)
                withContext(Dispatchers.Main) {
                    cameraConfig = cameraUiState.value.currentCameraConfig
                }
        }
    }

    override fun removeBackground() {
        _cameraUiState.update {cameraUiState ->
            cameraUiState.copy(
                currentCameraConfig = cameraUiState.currentCameraConfig.copy(
                    background = null,
                    backgroundMode = CameraConfig.BackgroundMode.Remove
                )
            )
        }
        cameraConfig = cameraUiState.value.currentCameraConfig
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
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                currentCameraConfig = cameraUiState.currentCameraConfig.copy(
                    blurPower = value
                )
            )
        }
        cameraConfig = cameraUiState.value.currentCameraConfig
    }

    override fun setZoomPower(value: Float) {
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                currentCameraConfig = cameraUiState.currentCameraConfig.copy(
                    smartZoom = (value*100).toInt()
                )
            )
        }

        cameraConfig = cameraUiState.value.currentCameraConfig
    }

    override fun setBeautifyPower(value: Float) {
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                currentCameraConfig = cameraUiState.currentCameraConfig.copy(
                    beautification = (value*100).toInt()
                )
            )
        }

        cameraConfig = cameraUiState.value.currentCameraConfig
    }

    override fun setColorCorrectionMode(mode: CameraConfig.ColorCorrection) {
        Log.d(TAG, "${mode.name} was chosen as color correction mode")
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                currentCameraConfig = cameraUiState.currentCameraConfig.copy(
                    colorCorrection = mode
                )
            )
        }

        cameraConfig = cameraUiState.value.currentCameraConfig
    }
}