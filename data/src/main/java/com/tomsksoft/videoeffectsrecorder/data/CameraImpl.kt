package com.tomsksoft.videoeffectsrecorder.data

import android.app.Activity
import android.util.Log
import com.effectssdk.tsvb.EffectsSDK
import com.effectssdk.tsvb.pipeline.CameraPipeline
import com.effectssdk.tsvb.pipeline.CameraPipelineImpl
import com.effectssdk.tsvb.pipeline.ColorCorrectionMode
import com.effectssdk.tsvb.pipeline.PipelineMode
import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.OnFrameListener

class CameraImpl(
    private val pipelineBuilder: CameraPipelineImpl.Builder
): Camera<Frame> {
    companion object {
        private val factory = EffectsSDK.createSDKFactory()
    }

    private val pipeline = pipelineBuilder.build()

    private val subscribers = ArrayList<OnFrameListener<Frame>>()

    constructor(context: Activity, camera: com.effectssdk.tsvb.Camera): this(
        factory.createCameraPipelineBuilder()
            .setContext(context)
            .setCamera(camera)
    )

    override var isEnabled: Boolean = false
        set(value) {
            if (field == value) return
            if (value) start() else release()
            field = value
        }

    override fun subscribe(listener: OnFrameListener<Frame>) {
        subscribers += listener
    }

    override fun unsubscribe(listener: OnFrameListener<Frame>) {
        subscribers -= listener
    }

    override fun configure(config: CameraConfig) = pipeline.run {
        /* Background Mode */
        when (config.backgroundMode) {
            is CameraConfig.BackgroundMode.Regular -> setMode(PipelineMode.NO_EFFECT)
            is CameraConfig.BackgroundMode.Replace -> TODO("[tva] background replace mode")
            is CameraConfig.BackgroundMode.Blur -> {
                setMode(PipelineMode.BLUR)
                setBlurPower((config.backgroundMode as CameraConfig.BackgroundMode.Blur).power)
            }
        }
        /* Smart Zoom */
        setZoomLevel(config.smartZoom?.faceSize ?: 0)
        /* Beautification */
        if (config.beautification != null) {
            enableBeautification(true)
            setBeautificationPower(config.beautification!!.power)
        } else enableBeautification(false)
        /* Color Correction */
        setColorCorrectionMode(when (config.colorCorrection) {
            CameraConfig.ColorCorrection.NO_FILTER -> ColorCorrectionMode.NO_FILTER_MODE
            CameraConfig.ColorCorrection.COLOR_CORRECTION -> ColorCorrectionMode.COLOR_CORRECTION_MODE
            CameraConfig.ColorCorrection.COLOR_GRADING -> ColorCorrectionMode.COLOR_GRADING_MODE
            CameraConfig.ColorCorrection.PRESET -> ColorCorrectionMode.PRESET_MODE
        })
    }

    /**
     * Creates instance prototype in default state
     */
    fun copy() = CameraImpl(pipelineBuilder)

    private fun start() {
        pipeline.startPipeline()
        pipeline.setOnFrameAvailableListener { bitmap ->
            subscribers.forEach { it.onFrame(Frame(bitmap)) }
        }
        pipeline.setOrientationChangeListener { orientation, rotation ->
            Log.d("Camera", "$orientation $rotation") // TODO [tva] notify UI to rotate icons
        }
    }

    /**
     * Camera can't be reused after this invocation
     */
    private fun release() {
        pipeline.release()
    }
}