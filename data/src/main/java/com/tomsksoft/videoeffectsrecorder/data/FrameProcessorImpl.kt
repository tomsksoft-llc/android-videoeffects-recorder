package com.tomsksoft.videoeffectsrecorder.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.Surface
import com.effectssdk.tsvb.Camera
import com.effectssdk.tsvb.EffectsSDK
import com.effectssdk.tsvb.pipeline.CameraPipeline
import com.effectssdk.tsvb.pipeline.ColorCorrectionMode
import com.effectssdk.tsvb.pipeline.OnFrameAvailableListener
import com.effectssdk.tsvb.pipeline.PipelineMode
import com.tomsksoft.videoeffectsrecorder.domain.BackgroundMode
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.ColorCorrection
import com.tomsksoft.videoeffectsrecorder.domain.FrameProcessor
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject

class FrameProcessorImpl(val context: Context): FrameProcessor, AutoCloseable, OnFrameAvailableListener {
    companion object {
        private val factory = EffectsSDK.createSDKFactory()
    }

    override val frameSource = PublishSubject.create<Any>()
    override val processedFrame = BehaviorSubject.create<Any>()
    override var direction: FrameProcessor.Direction = FrameProcessor.Direction.BACK
        set(value) {
            field = value
            pipeline.release()
            pipeline = start(
                context,
                when (field) {
                    FrameProcessor.Direction.BACK -> Camera.BACK
                    FrameProcessor.Direction.FRONT -> Camera.FRONT
                }
            )
        }
    private var pipeline: CameraPipeline
    private var surface: Surface? = null
/*    private val disposable = frameSource
        .map(FrameMapper::fromAny)
        .subscribe(pipeline::process)*/

    init {
        pipeline = start(context, Camera.BACK)
    }

    private fun start(context: Context, cameraDirection: Camera): CameraPipeline {
        val pipeline = factory.createCameraPipeline(
            context,
            fpsListener = { Log.d("FPS", it.toString()) },
            camera = cameraDirection
        )

        pipeline.setSegmentationGap(1)
        pipeline.setOnFrameAvailableListener(this)
        pipeline.setOutputSurface(surface)
        pipeline.startPipeline()
        return pipeline
    }

    override fun onNewFrame(bitmap: Bitmap) =
        processedFrame.onNext(FrameMapper.toAny(bitmap))

    override fun close() {
        //disposable.dispose()
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
            when (cameraConfig.colorCorrection) {
                ColorCorrection.NO_FILTER -> {setColorCorrectionMode(ColorCorrectionMode.NO_FILTER_MODE)}
                ColorCorrection.COLOR_CORRECTION -> {
                    setColorCorrectionMode(ColorCorrectionMode.COLOR_CORRECTION_MODE)
                    setColorFilterStrength(cameraConfig.colorCorrectionPower)
                }
                ColorCorrection.COLOR_GRADING -> {
                    setColorCorrectionMode(ColorCorrectionMode.COLOR_GRADING_MODE)
                    //setColorGradingReferenceImage(cameraConfig.colorGradingReference as Bitmap) TODO: убрать
                }
                ColorCorrection.PRESET -> {
                    setColorCorrectionMode(ColorCorrectionMode.PRESET_MODE)
                }
            }
        }
}