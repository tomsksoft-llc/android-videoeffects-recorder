package com.tomsksoft.videoeffectsrecorder.data

import android.content.Context
import android.graphics.Bitmap
import com.effectssdk.tsvb.EffectsSDK
import com.effectssdk.tsvb.pipeline.ColorCorrectionMode
import com.effectssdk.tsvb.pipeline.OnFrameAvailableListener
import com.effectssdk.tsvb.pipeline.PipelineMode
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.FrameProcessor
import io.reactivex.rxjava3.subjects.BehaviorSubject
import io.reactivex.rxjava3.subjects.PublishSubject

class FrameProcessorImpl(context: Context): FrameProcessor, OnFrameAvailableListener, AutoCloseable {
    companion object {
        private val factory = EffectsSDK.createSDKFactory()
    }

    override val frameSource = PublishSubject.create<Any>()
    override val processedFrame = BehaviorSubject.create<Any>()

    private val pipeline = factory.createImagePipeline(context)
    private val disposable = frameSource
        .map(FrameMapper::fromAny)
        .subscribe(pipeline::process)

    init {
        pipeline.setSegmentationGap(1)
        pipeline.setOnFrameAvailableListener(this)
    }

    override fun onNewFrame(bitmap: Bitmap) = processedFrame.onNext(FrameMapper.toAny(bitmap))

    override fun close() {
        disposable.dispose()
        pipeline.release()
    }

    override fun configure(cameraConfig: CameraConfig): Unit =
        pipeline.run {
            /* Background Mode */
            when (cameraConfig.backgroundMode) {
                CameraConfig.BackgroundMode.Regular -> setMode(PipelineMode.NO_EFFECT)
                CameraConfig.BackgroundMode.Remove -> setMode(PipelineMode.REMOVE)
                CameraConfig.BackgroundMode.Replace -> {
                    setMode(PipelineMode.REPLACE)
                    setBackground(cameraConfig.background as Bitmap)
                }
                CameraConfig.BackgroundMode.Blur -> {
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
                CameraConfig.ColorCorrection.NO_FILTER -> ColorCorrectionMode.NO_FILTER_MODE
                CameraConfig.ColorCorrection.COLOR_CORRECTION -> ColorCorrectionMode.COLOR_CORRECTION_MODE
                CameraConfig.ColorCorrection.COLOR_GRADING -> ColorCorrectionMode.COLOR_GRADING_MODE
                CameraConfig.ColorCorrection.PRESET -> ColorCorrectionMode.PRESET_MODE
            })
        }
}