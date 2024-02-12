package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.OnFrameListener

class CameraViewManager<T: Camera<F>, F>(
    camera: T,
    listener: OnFrameListener<F>
): OnFrameListener<F> by listener {

    var camera: T = camera
        set(value) {
            field.unsubscribe(this)
            value.subscribe(this)
            field = value
        }

    init {
        camera.subscribe(this)
    }
}