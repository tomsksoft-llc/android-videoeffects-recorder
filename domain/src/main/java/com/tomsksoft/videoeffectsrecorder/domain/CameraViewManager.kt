package com.tomsksoft.videoeffectsrecorder.domain

class CameraViewManager<T: Camera<F>, F>(
    camera: T,
    listener: Camera.OnFrameListener<F>
): Camera.OnFrameListener<F> by listener {

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