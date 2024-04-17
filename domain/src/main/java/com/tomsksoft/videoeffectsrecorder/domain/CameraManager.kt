package com.tomsksoft.videoeffectsrecorder.domain

import android.view.Surface
import io.reactivex.rxjava3.subjects.BehaviorSubject

class CameraManager(
    private val effectsPipelineCamera: EffectsPipelineCamera
): AutoCloseable {
    val frameSource by effectsPipelineCamera::processedFrame
    val orientation by effectsPipelineCamera::orientation
    var direction by effectsPipelineCamera::direction
    var isEnabled by effectsPipelineCamera::isEnabled
    var flashMode by effectsPipelineCamera::flashMode
    val cameraConfig = BehaviorSubject.createDefault(CameraConfig())

    private val disposable = cameraConfig.subscribe(effectsPipelineCamera::configure)

    fun setSurface(surface: Surface?) = effectsPipelineCamera.setSurface(surface)
    override fun close() = disposable.dispose()
}