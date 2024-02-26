package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomsksoft.videoeffectsrecorder.data.CameraImpl
import com.tomsksoft.videoeffectsrecorder.data.Frame
import com.tomsksoft.videoeffectsrecorder.data.VideoRecorderImpl
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraEffectsManager
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraRecordManager
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

private const val TAG = "Camera View Model"

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

    @Volatile
    private var background: Bitmap? = null

    private lateinit var cameraRecordManager: CameraRecordManager<Frame>
    private lateinit var cameraEffectsManager: CameraEffectsManager<CameraImpl>
    private lateinit var camera: CameraImpl

    fun initializeCamera(lifecycleOwner: LifecycleOwner, context: Activity) {
        camera = CameraImpl(lifecycleOwner, context, CameraSelector.DEFAULT_BACK_CAMERA)
        camera.frame.subscribe(_frame)
        cameraRecordManager = CameraRecordManager(
            camera,
            VideoRecorderImpl(context.applicationContext, RECORDS_DIRECTORY)
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
                Log.d(TAG, "Blur mode selected")
                backgroundMode = CameraConfig.BackgroundMode.Blur(0.5)  // TODO [fmv] add appropriate way to change blur power
            }
            FiltersMode.REPLACE_BACK -> {
                Log.d(TAG, "Background mode selected")
                backgroundMode = background?.let(CameraConfig.BackgroundMode::Replace)
                    ?: CameraConfig.BackgroundMode.Remove
            }
            FiltersMode.BEAUTIFY -> {
                Log.d(TAG, "Beatify mode selected")
                beautification = CameraConfig.Beautification(30) // TODO [fmv] add appropriate way to change beautification power
            }
            FiltersMode.SMART_ZOOM -> {
                Log.d(TAG, "Smart zoom mode selected")
                smartZoom = CameraConfig.SmartZoom(20) // TODO [fmv] add appropriate way to change face size
            }
            FiltersMode.COLOR_CORRECTION -> {
                Log.d(TAG, "Color correction mode selected")
                colorCorrection = CameraConfig.ColorCorrection.COLOR_CORRECTION
            }
            FiltersMode.NONE -> {
                Log.d(TAG, "NO mode selected")
            }
        }

        cameraEffectsManager.config = CameraConfig(
            backgroundMode = backgroundMode,
            smartZoom = smartZoom,
            beautification = beautification,
            colorCorrection = colorCorrection
        )

    }

    fun setBackground(bitmapStream: InputStream) { // TODO [tva] add bitmap property to UiState
        viewModelScope.launch {
            background = withContext(Dispatchers.IO) {
                bitmapStream.use(BitmapFactory::decodeStream)
            }
            if (_cameraUiState.value.filtersMode == FiltersMode.REPLACE_BACK)
                cameraEffectsManager.config = cameraEffectsManager.config.copy(
                    backgroundMode = CameraConfig.BackgroundMode.Replace(background!!)
                )
        }
    }

    fun removeBackground() {
        background = null
        cameraEffectsManager.config = cameraEffectsManager.config.copy(
            backgroundMode = CameraConfig.BackgroundMode.Remove
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
        camera.cameraSelector =
            if (camera.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                CameraSelector.DEFAULT_FRONT_CAMERA
            else
                CameraSelector.DEFAULT_BACK_CAMERA
    }

    fun captureImage(){
        Log.d(TAG, "Capture image")
        // TODO: [fmv] add usecase interaction
    }

    fun startVideoRecording(){
        Log.d(TAG, "Start recording")

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
        Log.d(TAG, "Stop recording")
        cameraRecordManager.isRecording = false
    }
}