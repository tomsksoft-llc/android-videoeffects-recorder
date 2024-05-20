package com.tomsksoft.videoeffectsrecorder.domain.mock

import com.tomsksoft.videoeffectsrecorder.domain.boundary.Camera
import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.entity.FlashMode
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

class CameraMock: Camera {

    override val orientation: Int = 0
    override val frame: Observable<Any> = Observable.interval(100, TimeUnit.MILLISECONDS)
        .flatMap {
            Observable.create { source ->
                if (isEnabled)
                    source.onNext("Frame $it")
                source.onComplete()
            }
        }
    @Volatile override var isEnabled: Boolean = false

    // for test purposes
    var direction: Camera.Direction = Camera.Direction.BACK
    var isFlashEnabled: Boolean = false
    lateinit var cameraConfig: CameraConfig

    override fun configure(cameraConfig: CameraConfig) {
        this.cameraConfig = cameraConfig
    }

    override fun setFlashEnabled(enabled: Boolean) {
        isFlashEnabled = true
    }

    override fun setDirection(direction: Camera.Direction) {
        this.direction = direction
    }
}