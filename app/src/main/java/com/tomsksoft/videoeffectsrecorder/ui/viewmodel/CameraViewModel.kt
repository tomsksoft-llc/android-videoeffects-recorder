package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.app.Activity
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomsksoft.videoeffectsrecorder.data.CameraImpl
import com.tomsksoft.videoeffectsrecorder.data.CameraStoreImpl
import com.tomsksoft.videoeffectsrecorder.data.Frame
import com.tomsksoft.videoeffectsrecorder.data.VideoRecorderImpl
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraEffectsManager
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraRecordManager
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraStoreManager
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraViewManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CameraViewModel: ViewModel() {

    private val _cameraUiState : MutableStateFlow<CameraUiState>
            = MutableStateFlow(CameraUiState(
        flashMode = FlashMode.AUTO,
        expandedTopBarMode = ExpandedTopBarMode.DEFAULT,
        filtersMode = FiltersMode.NONE,
        isVideoRecording = false,
        isCameraInitialized = false,)
    )
    val cameraUiState: StateFlow<CameraUiState> = _cameraUiState.asStateFlow()

    private val _frame = MutableStateFlow<Bitmap?>(null)
    val frame: StateFlow<Bitmap?> = _frame.asStateFlow()

    private lateinit var cameraStoreManager: CameraStoreManager<CameraImpl>
    private lateinit var cameraViewManager: CameraViewManager<CameraImpl, Frame>
    private lateinit var cameraRecordManager: CameraRecordManager<CameraImpl, Frame>
    private lateinit var cameraEffectsManager: CameraEffectsManager<CameraImpl>

    /**
     * Camera selected from store manager.
     * Managers will be update on camera change.
     */
    private var cameraIndex = 0
        set(value) {
            if (field == value) return
            camera.isEnabled = false
            field = value
            camera = selectCamera()
            camera.isEnabled = true
            cameraViewManager.camera = camera
            cameraRecordManager.camera = camera
            cameraEffectsManager.camera = camera
        }
    private lateinit var camera: CameraImpl

    fun initializeCamera(context: Activity) {
        cameraStoreManager = CameraStoreManager(CameraStoreImpl(context))
        camera = selectCamera()
        cameraViewManager = CameraViewManager(camera) { _frame.value = it.bitmap }
        cameraRecordManager = CameraRecordManager(camera, VideoRecorderImpl(context.applicationContext))
        cameraEffectsManager = CameraEffectsManager(
            camera,
            CameraConfig(
                backgroundMode = CameraConfig.BackgroundMode.Regular,
                smartZoom = null,
                beautification = null,
                colorCorrection = CameraConfig.ColorCorrection.NO_FILTER
            )
        )
        camera.isEnabled = true

        _cameraUiState.update {cameraUiState ->
            cameraUiState.copy(
                isCameraInitialized = true
            )
        }
    }

    fun setFlash(flashMode: FlashMode) {
        _cameraUiState.update{cameraUiState ->
            cameraUiState.copy(
                flashMode = flashMode
            )
        }
        // TODO [fmv]: add usecase interaction
    }

    fun setFilters(filtersMode: FiltersMode) {
        _cameraUiState.update {cameraUiState ->
            cameraUiState.copy(
                filtersMode = filtersMode
            )
        }

        var backgroundMode : CameraConfig.BackgroundMode = CameraConfig.BackgroundMode.Regular
        var smartZoom : CameraConfig.SmartZoom? = null
        var beautification : CameraConfig.Beautification? = null
        var colorCorrection : CameraConfig.ColorCorrection = CameraConfig.ColorCorrection.NO_FILTER

        when (filtersMode) {
            FiltersMode.BLUR -> {
                backgroundMode = CameraConfig.BackgroundMode.Blur(0.5)  // TODO [fmv] add appropriate way to change blur power
            }
            FiltersMode.REPLACE_BACK -> {
                //backgroundMode = CameraConfig.BackgroundMode.Replace() // waiting for useacase implementation
            }
            FiltersMode.BEAUTIFY -> {
                beautification = CameraConfig.Beautification(30) // TODO [fmv] add appropriate way to change beautification power
            }
            FiltersMode.SMART_ZOOM -> {
                smartZoom = CameraConfig.SmartZoom(20) // TODO [fmv] add appropriate way to change face size
            }
            FiltersMode.COLOR_CORRECTION -> {
                colorCorrection = CameraConfig.ColorCorrection.COLOR_CORRECTION
            }
            FiltersMode.NONE -> {}
        }

        cameraEffectsManager.config = CameraConfig(
            backgroundMode = backgroundMode,
            smartZoom = smartZoom,
            beautification = beautification,
            colorCorrection = colorCorrection
        )

    }


    fun toggleQuickSettingsIndicator(expandedTopBarMode: ExpandedTopBarMode){
        _cameraUiState.update {cameraUiState ->
            cameraUiState.copy(
                expandedTopBarMode = expandedTopBarMode
            )
        }

    }

    fun flipCamera(){
        cameraIndex = (cameraIndex + 1) % cameraStoreManager.camerasCount
    }

    fun captureImage(){
        // TODO: [fmv] add usecase interaction
    }

    fun startVideoRecording(){
        viewModelScope.launch{
            _cameraUiState.update {cameraUiState ->
                cameraUiState.copy(
                    isVideoRecording = true
                )
            }
            cameraRecordManager.isRecording = true
        }
    }

    fun stopVideoRecording() {
        viewModelScope.launch{
            _cameraUiState.update {cameraUiState ->
                cameraUiState.copy(
                    isVideoRecording = false
                )
            }
            cameraRecordManager.isRecording = false
        }
    }

    private fun selectCamera() = cameraStoreManager.cameras[cameraIndex].copy()
}