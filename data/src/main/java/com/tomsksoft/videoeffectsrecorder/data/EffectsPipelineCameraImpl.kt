package com.tomsksoft.videoeffectsrecorder.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.Surface
import com.effectssdk.tsvb.EffectsSDK
import com.effectssdk.tsvb.pipeline.CameraPipeline
import com.effectssdk.tsvb.pipeline.ColorCorrectionMode
import com.effectssdk.tsvb.pipeline.DeviceOrientation
import com.effectssdk.tsvb.pipeline.OnFrameAvailableListener
import com.effectssdk.tsvb.pipeline.OrientationChangeListener
import com.effectssdk.tsvb.pipeline.PipelineMode
import com.tomsksoft.videoeffectsrecorder.domain.BackgroundMode
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.ColorCorrection
import com.tomsksoft.videoeffectsrecorder.domain.EffectsPipelineCamera
import com.tomsksoft.videoeffectsrecorder.domain.Camera
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlin.math.abs

class EffectsPipelineCameraImpl(val context: Context): EffectsPipelineCamera, AutoCloseable, OnFrameAvailableListener {
    companion object {
        private val factory = EffectsSDK.createSDKFactory()
    }

    override val processedFrame = BehaviorSubject.create<Any>()
    override var orientation: Int = 0
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
    override var isEnabled: Boolean = false
    private var pipeline: CameraPipeline
    private var surface: Surface? = null

    init {
        pipeline = start(context, com.effectssdk.tsvb.Camera.BACK)
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
        processedFrame.onNext(FrameMapper.toAny(bitmap))

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
            setColorFilterStrength(1f) // TODO [tva] pass through config
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