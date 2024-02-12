package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig

class CameraEffectsManager<T: Camera<*>>(
    camera: T,
    config: CameraConfig
) {
    var camera = camera
        set(value) {
            field = value
            updateConfiguration()
        }

    var config = config
        set(value) {
            field = value
            updateConfiguration()
        }

    init { updateConfiguration() }

    private fun updateConfiguration() = camera.configure(config)
}