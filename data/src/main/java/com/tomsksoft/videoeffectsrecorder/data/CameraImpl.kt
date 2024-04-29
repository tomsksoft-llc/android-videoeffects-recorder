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
import com.tomsksoft.videoeffectsrecorder.domain.entity.FlashMode
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.math.abs

class CameraImpl(val context: Context): Camera, AutoCloseable, OnFrameAvailableListener, LifecycleOwner {
    companion object {
        private val factory = EffectsSDK.createSDKFactory()
    }

    override var lifecycle = LifecycleRegistry(this)
    override val frame = BehaviorSubject.create<Any>()
    override var orientation: Int = 0
    override var flashMode: FlashMode = FlashMode.OFF
        set(value) {
            when (value) {
                FlashMode.ON -> cam.cameraControl.enableTorch(true)
                FlashMode.OFF -> cam.cameraControl.enableTorch(false)
                FlashMode.AUTO -> cam.cameraControl.enableTorch(false)
            }
            field = value
        }
    override var direction: Camera.Direction = Camera.Direction.BACK
        set(value) {
            field = value
            pipeline.release()
            isEnabled = false
            pipeline = start(
                context,
                when (field) {
                    Camera.Direction.BACK -> com.effectssdk.tsvb.Camera.BACK
                    Camera.Direction.FRONT -> com.effectssdk.tsvb.Camera.FRONT
                }
            )
        }
    override var isFlashEnabled: Boolean = false
        set(value) {
            when (flashMode) {
                FlashMode.ON -> return
                FlashMode.OFF -> return
                FlashMode.AUTO -> {
                    field = value
                    cam.cameraControl.enableTorch(value)
                }
            }
        }
    override var isEnabled: Boolean = false
    private var pipeline: CameraPipeline
    private var surface: Surface? = null
    private val cam = ProcessCameraProvider
        .getInstance(context).get()
        .bindToLifecycle(
            this,
            CameraSelector.DEFAULT_BACK_CAMERA)

    init {
        pipeline = start(context, com.effectssdk.tsvb.Camera.BACK)
        lifecycle.currentState = Lifecycle.State.CREATED
    }

    private fun start(context: Context, cameraDirection: com.effectssdk.tsvb.Camera): CameraPipeline {
        val pipeline = factory.createCameraPipeline(
            context,
            fpsListener = { Log.d("FPS", it.toString()) },
            camera = cameraDirection
        )

        pipeline.setSegmentationGap(1)
        pipeline.setOnFrameAvailableListener(this)
        pipeline.setOrientationChangeListener(OrientationChangeListenerImpl())
        pipeline.setOutputSurface(surface)
        pipeline.startPipeline()
        isEnabled = true
        return pipeline
    }

    override fun onNewFrame(bitmap: Bitmap) =
        frame.onNext(FrameMapper.toAny(bitmap))

    override fun close() {
        isEnabled = false
        pipeline.release()
    }

    override fun setSurface(surface: Surface?) {
        this.surface = surface
        pipeline.setOutputSurface(this.surface)
    }

    override fun configure(cameraConfig: CameraConfig): Unit =
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
    inner class OrientationChangeListenerImpl: OrientationChangeListener {
        override fun onOrientationChanged(deviceOrientation: DeviceOrientation, rotation: Int) {
            val degree = when {
                abs(rotation - 90) <= 45 -> 90
                abs(rotation - 180) <= 45 -> 180
                abs(rotation - 270) <= 45 -> 270
                else -> 0
            }
            if (degree != rotation)
                orientation = degree
        }
    }
}