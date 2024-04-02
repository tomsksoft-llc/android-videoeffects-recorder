package com.tomsksoft.videoeffectsrecorder.domain

import android.view.Surface
import io.reactivex.rxjava3.subjects.BehaviorSubject

class CameraManager(
    private val camera: Camera,
    private val frameProcessor: FrameProcessor
): AutoCloseable {
    val frameSource by frameProcessor::processedFrame
    val orientation by camera::orientation
    var direction by camera::direction
    var isEnabled by camera::isEnabled
    val cameraConfig = BehaviorSubject.createDefault(CameraConfig())

    private val disposable = cameraConfig.subscribe(frameProcessor::configure)

    init {
        camera.frameSource.subscribe(frameProcessor.frameSource)
    }

    fun setSurface(surface: Surface?) = frameProcessor.setSurface(surface)
    override fun close() = disposable.dispose()
}