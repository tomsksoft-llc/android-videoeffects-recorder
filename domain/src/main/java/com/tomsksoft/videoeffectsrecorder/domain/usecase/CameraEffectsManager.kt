package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.CameraConfigurer

class CameraEffectsManager<T: Camera<*>>(
    camera: T,
    config: CameraConfig,
    private val configurer: CameraConfigurer<T>
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

    private fun updateConfiguration() = configurer.configure(camera, config)
}