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
import com.tomsksoft.videoeffectsrecorder.domain.boundary.Camera
import io.reactivex.rxjava3.subjects.BehaviorSubject

class CameraImpl(
    private val context: Context
): AndroidCamera, AutoCloseable, OnFrameAvailableListener, LifecycleOwner {
    companion object {
        private const val TAG = "Camera"
        private val factory = EffectsSDK.createSDKFactory()
    }

    override var lifecycle = LifecycleRegistry(this)
    override val frame = BehaviorSubject.create<Any>()
    override var orientation: Int = 0
    override var isEnabled: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value)
                createPipeline()
            else
                releasePipeline()
        }

    /**
     * Cached direction for {@link createPipeline}
     */
    private var _direction = Camera.Direction.BACK
    /**
     * Updates only by {@link releasePipeline} and {@link createPipeline}
     */
    private var pipeline: CameraPipeline? = null
    private var surface: Surface? = null
    private val cam = ProcessCameraProvider
        .getInstance(context).get()
        .bindToLifecycle(
            this,
            CameraSelector.DEFAULT_BACK_CAMERA
        )

    init {
        lifecycle.currentState = Lifecycle.State.CREATED
    }

    /**
     * Updates pipeline property with new instance
     * @see pipeline
     */
    private fun createPipeline() {
        Log.d(TAG, "Create pipeline")

        val pipeline = factory.createCameraPipeline(
            context,
            fpsListener = { Log.d("FPS", it.toString()) },
            camera = when (_direction) {
                Camera.Direction.BACK -> com.effectssdk.tsvb.Camera.BACK
                Camera.Direction.FRONT -> com.effectssdk.tsvb.Camera.FRONT
            }
        )

        pipeline.setSegmentationGap(1)
        pipeline.setOnFrameAvailableListener(this)
        pipeline.setOrientationChangeListener(OrientationChangeListenerImpl())
        pipeline.setOutputSurface(surface)
        pipeline.startPipeline()
        this.pipeline = pipeline
    }

    private fun releasePipeline() {
        Log.d(TAG, "Release pipeline")
        pipeline?.release()
        pipeline = null
    }

    override fun onNewFrame(bitmap: Bitmap) =
        frame.onNext(FrameMapper.toAny(bitmap))

    override fun close() {
        isEnabled = false
        lifecycle.currentState = Lifecycle.State.DESTROYED // unbind CameraX
        releasePipeline()
    }

    override fun setSurface(surface: Surface?) {
        this.surface = surface
        pipeline?.setOutputSurface(this.surface)
    }

    override fun setFlashEnabled(enabled: Boolean) {
        cam.cameraControl.enableTorch(enabled)
    }

    override fun setDirection(direction: Camera.Direction) {
        if (direction == this._direction)
            return

        this._direction = direction

        if (isEnabled) { // recreate pipeline for relevant direction
            releasePipeline()
            createPipeline()
        }
    }

    override fun configure(cameraConfig: CameraConfig): Unit {
        pipeline?.run {
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

    inner class OrientationChangeListenerImpl: OrientationChangeListener {
        override fun onOrientationChanged(deviceOrientation: DeviceOrientation, rotation: Int) {
            orientation = 360 - rotation // rotation in this callback is counted counter-clockwise, but clockwise degrees are required for android.media.MediaRecorder
        }
    }
}