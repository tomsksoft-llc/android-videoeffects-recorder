package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.boundary.Camera
import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.entity.FlashMode
import io.reactivex.rxjava3.subjects.BehaviorSubject

class CameraManager(
    val camera: Camera
): AutoCloseable {
    val frameSource by camera::frame
    val orientation by camera::orientation

    var direction: Camera.Direction = Camera.Direction.FRONT
        set(value) {
            if (field == value) return
            field = value
            camera.setDirection(value)
        }

    var flashMode = FlashMode.AUTO
        set(value) {
            when (value) {
                FlashMode.ON -> camera.setFlashEnabled(true)
                FlashMode.OFF -> camera.setFlashEnabled(false)
                FlashMode.AUTO -> camera.setFlashEnabled(false)
            }
            field = value
        }

    var isFlashEnabled = false // depends on FlashMode
        get() {
            return when (flashMode) {
                FlashMode.ON -> true
                FlashMode.OFF -> false
                FlashMode.AUTO -> field
            }
        }
        set(value) {
            when (flashMode) {
                FlashMode.ON -> return
                FlashMode.OFF -> return
                FlashMode.AUTO -> {
                    field = value
                    camera.setFlashEnabled(value)
                }
            }
        }

    val cameraConfig = BehaviorSubject.createDefault(CameraConfig())

    private val disposable = cameraConfig.subscribe(camera::configure)

    init {
        camera.apply {
            setDirection(direction)
            setFlashEnabled(false)
        }
    }

    override fun close() = disposable.dispose()
}