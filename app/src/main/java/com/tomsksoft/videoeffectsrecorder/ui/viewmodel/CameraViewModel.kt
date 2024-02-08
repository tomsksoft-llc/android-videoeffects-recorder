package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.app.Activity
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.tomsksoft.videoeffectsrecorder.data.CameraConfigurerImpl
import com.tomsksoft.videoeffectsrecorder.data.CameraImpl
import com.tomsksoft.videoeffectsrecorder.data.CameraStoreImpl
import com.tomsksoft.videoeffectsrecorder.data.Frame
import com.tomsksoft.videoeffectsrecorder.data.VideoRepositoryImpl
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraEffectsManager
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraRecordManager
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraStoreManager
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraViewManager
import kotlinx.coroutines.flow.MutableStateFlow

class CameraViewModel: ViewModel() {

    val flashMode = MutableStateFlow(FlashMode.AUTO)
    val frame = MutableStateFlow<Bitmap?>(null)

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
            camera.isEnabled = true
            cameraViewManager.camera = camera
            cameraRecordManager.camera = camera
            cameraEffectsManager.camera = camera
        }
    private val camera: CameraImpl
        get() = cameraStoreManager.cameras[cameraIndex]

    fun initializeCamera(context: Activity) {
        cameraStoreManager = CameraStoreManager(CameraStoreImpl(context))
        cameraViewManager = CameraViewManager(camera) { frame.value = it.bitmap }
        cameraRecordManager = CameraRecordManager(camera, VideoRepositoryImpl())
        cameraEffectsManager = CameraEffectsManager(
            camera,
            CameraConfig(
                backgroundMode = CameraConfig.BackgroundMode.Regular,
                smartZoom = null,
                beautification = null,
                colorCorrection = CameraConfig.ColorCorrection.NO_FILTER
            ),
            CameraConfigurerImpl()
        )
        camera.isEnabled = true
    }

    fun changeFlashMode() {
        val values = FlashMode.values()
        flashMode.value = values[(values.indexOf(flashMode.value) + 1) % values.size] // next mode
    }

    fun switchCamera() {
        cameraIndex = (cameraIndex + 1) % cameraStoreManager.camerasCount
    }

    enum class FlashMode {
        AUTO,
        ON,
        OFF
    }
}