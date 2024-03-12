package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tomsksoft.videoeffectsrecorder.data.CameraImpl
import com.tomsksoft.videoeffectsrecorder.data.FrameMapper
import com.tomsksoft.videoeffectsrecorder.data.VideoRecorderImpl
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.CameraRecordManager
import com.tomsksoft.videoeffectsrecorder.domain.DEFAULT_CAMERA_CONFIG
import com.tomsksoft.videoeffectsrecorder.domain.PipelineConfigManager
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

class CameraViewModelImpl(app: Application): AndroidViewModel(app), ICameraViewModel {
    companion object {
        const val RECORDS_DIRECTORY = "Effects"
        private const val TAG = "Camera View Model"
    }

    private val _cameraUiState : MutableStateFlow<CameraUiState>
            = MutableStateFlow(CameraUiState())
    override val cameraUiState: StateFlow<CameraUiState> = _cameraUiState.asStateFlow()

    private val _frame = BehaviorSubject.create<Bitmap>()
    override val frame: Observable<Bitmap> = _frame.observeOn(AndroidSchedulers.mainThread())

    private val context: Context
        get() = getApplication<Application>().applicationContext

    private val camera = CameraImpl(context, CameraSelector.DEFAULT_BACK_CAMERA)
    private val cameraRecordManager = CameraRecordManager(
        camera,
        VideoRecorderImpl(context, RECORDS_DIRECTORY)
    )

    private val pipelineConfigManager = PipelineConfigManager(camera)
    override val cameraConfig: StateFlow<CameraConfig> = pipelineConfigManager.cameraConfig

    init {
        camera.apply {
            frame.map(FrameMapper::fromAny).subscribe(_frame)
            //configure(cameraConfig.value)
            isEnabled = true
        }
        pipelineConfigManager.configurePipeline()
    }

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
            PrimaryFiltersMode.BLUR -> pipelineConfigManager.configurePipeline(
                backgroundMode = CameraConfig.BackgroundMode.Blur,
                blurPower = 0f
            )
            PrimaryFiltersMode.REPLACE_BACK -> pipelineConfigManager.configurePipeline(
                backgroundMode =
                    if (cameraConfig.value.background == null)
                        CameraConfig.BackgroundMode.Remove
                    else CameraConfig.BackgroundMode.Replace
            )
            PrimaryFiltersMode.COLOR_CORRECTION -> pipelineConfigManager.configurePipeline(
                backgroundMode = CameraConfig.BackgroundMode.Regular,
                colorCorrection = CameraConfig.ColorCorrection.COLOR_GRADING
            )
            PrimaryFiltersMode.NONE -> pipelineConfigManager.configurePipeline(
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
            SecondaryFiltersMode.BEAUTIFY -> pipelineConfigManager.configurePipeline(
                beautification =
                    if (cameraUiState.value.isBeautifyEnabled) 0
                    else cameraConfig.value.beautification
            )
            SecondaryFiltersMode.SMART_ZOOM -> pipelineConfigManager.configurePipeline(
                smartZoom =
                    if (cameraUiState.value.isSmartZoomEnabled) 0
                    else cameraConfig.value.smartZoom
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
                    pipelineConfigManager.configurePipeline(
                        background = background,
                        backgroundMode = CameraConfig.BackgroundMode.Replace
                    )
                }
        }
    }

    override fun removeBackground() {
        pipelineConfigManager.configurePipeline(
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
        camera.cameraSelector =
            if (camera.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA)
                CameraSelector.DEFAULT_FRONT_CAMERA
            else
                CameraSelector.DEFAULT_BACK_CAMERA
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
        pipelineConfigManager.configurePipeline(blurPower = value)
    }

    override fun setZoomPower(value: Float) {
        pipelineConfigManager.configurePipeline(
            smartZoom = (value*100).toInt()
        )
    }

    override fun setBeautifyPower(value: Float) {
        pipelineConfigManager.configurePipeline(beautification = (value*100).toInt())
    }

    override fun setColorCorrectionMode(mode: CameraConfig.ColorCorrection) {
        Log.d(TAG, "${mode.name} was chosen as color correction mode")
        pipelineConfigManager.configurePipeline(colorCorrection = mode)
    }
}