package com.tomsksoft.videoeffectsrecorder.domain.mock

import com.tomsksoft.videoeffectsrecorder.domain.boundary.Camera
import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.entity.FlashMode
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class CameraMock: Camera {

    override val orientation: Int = 0
    override var direction: Camera.Direction = Camera.Direction.BACK
    @Volatile override var isEnabled: Boolean = false

    override val frame: Observable<Any> = Observable.interval(100, TimeUnit.MILLISECONDS)
        .flatMap {
            Observable.create { source ->
                if (isEnabled)
                    source.onNext("Frame $it")
                source.onComplete()
            }
        }

    override var flashMode: FlashMode = FlashMode.AUTO
        set(value) {
            field = value
            if (value == FlashMode.AUTO)
                isFlashEnabled = false
        }

    override var isFlashEnabled: Boolean = false
        get() = when (flashMode) {
            FlashMode.AUTO -> field
            FlashMode.ON -> true
            FlashMode.OFF -> false
        }
        set(value) {
            if (flashMode == FlashMode.AUTO)
                field = value
        }

    lateinit var cameraConfig: CameraConfig // for test purposes

    override fun configure(cameraConfig: CameraConfig) {
        this.cameraConfig = cameraConfig
    }
}