package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.app.Activity
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import androidx.lifecycle.ViewModel
import com.tomsksoft.videoeffectsrecorder.data.CameraImpl
import com.tomsksoft.videoeffectsrecorder.data.CameraStoreImpl
import com.tomsksoft.videoeffectsrecorder.data.VideoStore
import com.tomsksoft.videoeffectsrecorder.data.Frame
import com.tomsksoft.videoeffectsrecorder.data.VideoRecorderImpl
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraEffectsManager
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraRecordManager
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraStoreManager
import com.tomsksoft.videoeffectsrecorder.domain.usecase.FileManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.File

class CameraViewModel: ViewModel() {
    companion object {
        const val RECORDS_DIRECTORY = "Effects"
    }

    private val _cameraUiState : MutableStateFlow<CameraUiState>
            = MutableStateFlow(CameraUiState(
        flashMode = FlashMode.AUTO,
        expandedTopBarMode = ExpandedTopBarMode.DEFAULT,
        filtersMode = FiltersMode.NONE,
        isVideoRecording = false,
        isCameraInitialized = false,)
    )
    val cameraUiState: StateFlow<CameraUiState> = _cameraUiState.asStateFlow()

    private val _frame = BehaviorSubject.create<Frame>()
    val frame: Observable<Bitmap> = _frame
        .map(Frame::bitmap)
        .observeOn(AndroidSchedulers.mainThread())

    private lateinit var cameraStoreManager: CameraStoreManager<CameraImpl>
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
            cameraRecordManager.camera = camera
            cameraEffectsManager.camera = camera
        }
    private lateinit var camera: CameraImpl

    fun initializeCamera(context: Activity) {
        val recordsDir = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            RECORDS_DIRECTORY
        )
        cameraStoreManager = CameraStoreManager(CameraStoreImpl(context))
        camera = selectCamera()
        cameraRecordManager = CameraRecordManager(
            camera,
            FileManager(VideoStore(recordsDir)),
            VideoRecorderImpl(context.applicationContext)
        )
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
                backgroundMode = CameraConfig.BackgroundMode.Replace()
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
        Log.d("Camera View Model", "Capture image")
        // TODO: [fmv] add usecase interaction
    }

    fun startVideoRecording(){
        Log.d("Camera View Model", "Start recording")

        _cameraUiState.update {cameraUiState ->
            cameraUiState.copy(
                isVideoRecording = true
            )
        }
        cameraRecordManager.isRecording = true

    }

    fun stopVideoRecording() {
        _cameraUiState.update {cameraUiState ->
            cameraUiState.copy(
                isVideoRecording = false
            )
        }
        Log.d("Camera View Model", "Stop recording")
        cameraRecordManager.isRecording = false
    }

    private fun selectCamera(): CameraImpl {
        val camera = cameraStoreManager.cameras[cameraIndex].copy()
        camera.frame.subscribe(_frame)
        return camera
    }
}