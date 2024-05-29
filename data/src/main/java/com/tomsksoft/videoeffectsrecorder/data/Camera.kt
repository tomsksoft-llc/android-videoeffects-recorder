package com.tomsksoft.videoeffectsrecorder.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.effectssdk.tsvb.EffectsSDK
import com.effectssdk.tsvb.pipeline.CameraPipeline
import com.effectssdk.tsvb.pipeline.ColorCorrectionMode
import com.effectssdk.tsvb.pipeline.DeviceOrientation
import com.effectssdk.tsvb.pipeline.OnFrameAvailableListener
import com.effectssdk.tsvb.pipeline.OrientationChangeListener
import com.effectssdk.tsvb.pipeline.PipelineMode
import com.tomsksoft.videoeffectsrecorder.domain.entity.BackgroundMode
import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.entity.ColorCorrection
import com.tomsksoft.videoeffectsrecorder.domain.entity.Direction
import com.tomsksoft.videoeffectsrecorder.domain.usecase.CameraManager
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.BehaviorSubject

class Camera(
    private val context: Context,
    private val manager: CameraManager,
    direction: Direction = manager.direction.blockingFirst()
): AutoCloseable, OnFrameAvailableListener, LifecycleOwner {
    companion object {
        private const val TAG = "Camera"
        private val factory = EffectsSDK.createSDKFactory()
    }

    override var lifecycle = LifecycleRegistry(this)

    private val _frame = BehaviorSubject.create<Any>()
    val frame = _frame.hide()

    private var pipeline: CameraPipeline = createPipeline(direction)
    private var surface: Surface? = null
    private val cam = ProcessCameraProvider
        .getInstance(context).get()
        .bindToLifecycle(
            this,
            CameraSelector.DEFAULT_BACK_CAMERA
        )
    private val disposables: CompositeDisposable // subscription to manager

    init {
        disposables = CompositeDisposable(
            manager.cameraConfig.subscribe(this::configure),
            manager.isFlashEnabled.subscribe(this::setFlashEnabled)
        )
        lifecycle.currentState = Lifecycle.State.CREATED
    }

    override fun onNewFrame(bitmap: Bitmap) = _frame.onNext(FrameMapper.toAny(bitmap))

    override fun close() {
        disposables.dispose()
        pipeline.release()
        lifecycle.currentState = Lifecycle.State.DESTROYED // unbind CameraX
    }

    fun setSurface(surface: Surface?) {
        this.surface = surface
        pipeline.setOutputSurface(this.surface)
    }

    private fun createPipeline(direction: Direction): CameraPipeline =
        factory.createCameraPipeline(
            context,
            fpsListener = { Log.d("FPS", it.toString()) },
            camera = when (direction) {
                Direction.BACK -> com.effectssdk.tsvb.Camera.BACK
                Direction.FRONT -> com.effectssdk.tsvb.Camera.FRONT
            }
        ).apply {
            setSegmentationGap(1)
            setOnFrameAvailableListener(this@Camera)
            setOrientationChangeListener(OrientationChangeListenerImpl())
            setOutputSurface(surface)
            startPipeline()
        }

    private fun setFlashEnabled(enabled: Boolean) {
        cam.cameraControl.enableTorch(enabled)
    }

    private fun configure(cameraConfig: CameraConfig) {
        pipeline.run {
            /* Background Mode */
            when (cameraConfig.backgroundMode) {
                BackgroundMode.Regular -> setMode(PipelineMode.NO_EFFECT)
                BackgroundMode.Remove -> setMode(PipelineMode.REMOVE)
                BackgroundMode.Replace -> {
                    setMode(PipelineMode.REPLACE)
                    setBackground(cameraConfig.background as Bitmap)
                }
                BackgroundMode.Blur -> {
                    setMode(PipelineMode.BLUR)
                    setBlurPower(cameraConfig.blurPower)
                }
            }
            /* Smart Zoom */
            setZoomLevel(cameraConfig.smartZoom ?: 0)
            /* Beautification */
            if (cameraConfig.beautification != null) {
                enableBeautification(true)
                setBeautificationPower(cameraConfig.beautification!!)
            } else enableBeautification(false)
            /* Color Correction */
            setColorCorrectionMode(when (cameraConfig.colorCorrection) {
                ColorCorrection.NO_FILTER -> ColorCorrectionMode.NO_FILTER_MODE
                ColorCorrection.COLOR_CORRECTION -> ColorCorrectionMode.COLOR_CORRECTION_MODE
                ColorCorrection.COLOR_GRADING -> ColorCorrectionMode.COLOR_GRADING_MODE
                ColorCorrection.PRESET -> ColorCorrectionMode.PRESET_MODE
            })
            if (
                cameraConfig.colorCorrection == ColorCorrection.COLOR_GRADING &&
                cameraConfig.colorGradingSource != null
            ) setColorGradingReferenceImage(cameraConfig.colorGradingSource as Bitmap)
            setColorFilterStrength(cameraConfig.colorCorrectionPower)
            /* Sharpness */
            setSharpeningStrength(cameraConfig.sharpnessPower ?: 0f)
        }
    }

    private inner class OrientationChangeListenerImpl: OrientationChangeListener {
        override fun onOrientationChanged(deviceOrientation: DeviceOrientation, rotation: Int) {
            // rotation in this callback is counted counter-clockwise,
            // but clockwise degrees are required for android.media.MediaRecorder
            val orientation = (360 - rotation) % 360
            manager.setOrientation(orientation)
            Log.d(TAG, "Orientation changed: $orientation")
        }
    }
}