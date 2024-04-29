package com.tomsksoft.videoeffectsrecorder.domain.usecase

import android.view.Surface
import com.tomsksoft.videoeffectsrecorder.domain.boundary.Camera
import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import io.reactivex.rxjava3.subjects.BehaviorSubject

class CameraManager(
    private val camera: Camera
): AutoCloseable {
    val frameSource by camera::frame
    val orientation by camera::orientation
    var direction by camera::direction
    var isEnabled by camera::isEnabled
    var flashMode by camera::flashMode
    var isFlashEnabled by camera::isFlashEnabled
    val cameraConfig = BehaviorSubject.createDefault(CameraConfig())

    private val disposable = cameraConfig.subscribe(camera::configure)

    fun setSurface(surface: Surface?) = camera.setSurface(surface)
    override fun close() = disposable.dispose()
}