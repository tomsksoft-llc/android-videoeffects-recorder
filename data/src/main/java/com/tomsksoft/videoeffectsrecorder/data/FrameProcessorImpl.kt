package com.tomsksoft.videoeffectsrecorder.data

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.view.Surface
import com.effectssdk.tsvb.EffectsSDK
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

class FrameProcessorImpl(context: Context): FrameProcessor, AutoCloseable, OnFrameAvailableListener {
    companion object {
        private val factory = EffectsSDK.createSDKFactory()
    }

    override val frameSource = PublishSubject.create<Any>()
    override val processedFrame = BehaviorSubject.create<Any>()

    private val pipeline = factory.createImagePipeline(
        context,
        fpsListener = { Log.d("FPS", it.toString()) }
    )
    private val disposable = frameSource
        .map(FrameMapper::fromAny)
        .subscribe(pipeline::process)

    init {
        pipeline.setSegmentationGap(1)
        pipeline.setOnFrameAvailableListener(this)
    }

    override fun onNewFrame(bitmap: Bitmap) =
        processedFrame.onNext(FrameMapper.toAny(bitmap))

    override fun close() {
        disposable.dispose()
        pipeline.release()
    }

    override fun setSurface(surface: Surface?) = pipeline.setOutputSurface(surface)

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
            setColorFilterStrength(1f) // TODO [tva] set it
        }
}