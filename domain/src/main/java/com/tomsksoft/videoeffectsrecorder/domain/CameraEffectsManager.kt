package com.tomsksoft.videoeffectsrecorder.domain

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

    private fun updateConfiguration() = configurer.configure(camera, config)
}