package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tomsksoft.videoeffectsrecorder.data.CameraImpl
import com.tomsksoft.videoeffectsrecorder.data.CameraStoreImpl
import com.tomsksoft.videoeffectsrecorder.data.VideoStore
import com.tomsksoft.videoeffectsrecorder.data.Frame
import com.tomsksoft.videoeffectsrecorder.data.VideoRecorderImpl
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraEffectsManager
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraRecordManager
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraStoreManager
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
        primaryFiltersMode = PrimaryFiltersMode.NONE,
        isSmartZoomEnabled = false,
        isBeautifyEnabled = false,
        isVideoRecording = false,
        isCameraInitialized = false,)
    )
    val cameraUiState: StateFlow<CameraUiState> = _cameraUiState.asStateFlow()

    private val _frame = BehaviorSubject.create<Frame>()
    val frame: Observable<Bitmap> = _frame
        .map(Frame::bitmap)
        .observeOn(AndroidSchedulers.mainThread())

    private val cameraConfigData = CameraConfigData(
        backgroundMode = CameraConfig.BackgroundMode.Regular,
        smartZoom = null,
        beautification = null,
        colorCorrection = CameraConfig.ColorCorrection.NO_FILTER
    )

    @Volatile
    private var background: Bitmap? = null

    private lateinit var cameraStoreManager: CameraStoreManager<CameraImpl>
    private lateinit var cameraRecordManager: CameraRecordManager<CameraImpl, Frame, ParcelFileDescriptor>
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
        cameraStoreManager = CameraStoreManager(CameraStoreImpl(context))
        camera = selectCamera()
        cameraRecordManager = CameraRecordManager(
            camera,
            VideoStore(context.applicationContext, RECORDS_DIRECTORY),
            VideoRecorderImpl(context.applicationContext)
        )
        cameraEffectsManager = CameraEffectsManager(
            camera,
            CameraConfig(
                cameraConfigData.backgroundMode,
                cameraConfigData.smartZoom,
                cameraConfigData.beautification,
                cameraConfigData.colorCorrection
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

    fun setPrimaryFilter(filtersMode: PrimaryFiltersMode) {
        _cameraUiState.update { cameraUiState ->
            cameraUiState.copy(
                primaryFiltersMode = filtersMode
            )
        }
        when (filtersMode) {
            //primary options
            PrimaryFiltersMode.BLUR -> {
                Log.d(TAG, "Blur mode selected")
                cameraConfigData.backgroundMode = CameraConfig.BackgroundMode.Blur(0.5)  // TODO [fmv] add an appropriate way to change blur power
                cameraConfigData.colorCorrection = CameraConfig.ColorCorrection.NO_FILTER
            }
            PrimaryFiltersMode.REPLACE_BACK -> {
                Log.d(TAG, "Background mode selected")
                cameraConfigData.backgroundMode = background?.let(CameraConfig.BackgroundMode::Replace)
                    ?: CameraConfig.BackgroundMode.Remove
                cameraConfigData.colorCorrection = CameraConfig.ColorCorrection.NO_FILTER
            }
            PrimaryFiltersMode.COLOR_CORRECTION -> {
                Log.d(TAG, "Color correction mode selected")
                cameraConfigData.backgroundMode = CameraConfig.BackgroundMode.Regular
                cameraConfigData.colorCorrection = CameraConfig.ColorCorrection.COLOR_CORRECTION    // TODO [fmv] add an appropriate way to change color correction modes
            }
            PrimaryFiltersMode.NONE -> {
                cameraConfigData.backgroundMode = CameraConfig.BackgroundMode.Regular
                cameraConfigData.colorCorrection = CameraConfig.ColorCorrection.NO_FILTER
                Log.d(TAG, "NO mode selected")
            }
        }

        cameraEffectsManager.config = CameraConfig(
            backgroundMode = cameraConfigData.backgroundMode,
            smartZoom = cameraConfigData.smartZoom,
            beautification = cameraConfigData.beautification,
            colorCorrection = cameraConfigData.colorCorrection
        )
    }

    fun setSecondaryFilters(filtersMode: SecondaryFiltersMode) {
        when (filtersMode) {
            SecondaryFiltersMode.BEAUTIFY -> {
                if (cameraConfigData.beautification != null) {
                    Log.d(TAG, "Beatify mode disabled")
                    cameraConfigData.beautification = null
                } else {
                    Log.d(TAG, "Beatify mode enabled")
                    cameraConfigData.beautification = CameraConfig.Beautification(30) // TODO [fmv] add an appropriate way to change beautification power
                }

                _cameraUiState.update { cameraUiState ->
                    cameraUiState.copy(
                        isBeautifyEnabled = !cameraUiState.isBeautifyEnabled
                    )
                }
            }

            SecondaryFiltersMode.SMART_ZOOM -> {
                if (cameraConfigData.smartZoom != null) {
                    Log.d(TAG, "Smart Zoom mode disabled")
                    cameraConfigData.smartZoom = null
                } else {
                    Log.d(TAG, "Smart Zoom mode enabled")
                    cameraConfigData.smartZoom = CameraConfig.SmartZoom(80) // TODO [fmv] add an appropriate way to change beautification power
                }
                _cameraUiState.update { cameraUiState ->
                    cameraUiState.copy(
                        isSmartZoomEnabled = !cameraUiState.isSmartZoomEnabled
                    )
                }
            }
        }

        cameraEffectsManager.config = CameraConfig(
            backgroundMode = cameraConfigData.backgroundMode,
            smartZoom = cameraConfigData.smartZoom,
            beautification = cameraConfigData.beautification,
            colorCorrection = cameraConfigData.colorCorrection
        )
    }
    fun setBackground(bitmapStream: InputStream) { // TODO [tva] add bitmap property to UiState
        viewModelScope.launch {
            background = withContext(Dispatchers.IO) {
                bitmapStream.use(BitmapFactory::decodeStream)
            }
            if (_cameraUiState.value.primaryFiltersMode == PrimaryFiltersMode.REPLACE_BACK)
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
        cameraIndex = (cameraIndex + 1) % cameraStoreManager.camerasCount
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

    private fun selectCamera(): CameraImpl {
        val camera = cameraStoreManager.cameras[cameraIndex].copy()
        camera.frame.subscribe(_frame)
        return camera
    }
}

data class CameraConfigData(
    var backgroundMode: CameraConfig.BackgroundMode,
    var smartZoom: CameraConfig.SmartZoom?,
    var beautification: CameraConfig.Beautification?,
    var colorCorrection: CameraConfig.ColorCorrection
)