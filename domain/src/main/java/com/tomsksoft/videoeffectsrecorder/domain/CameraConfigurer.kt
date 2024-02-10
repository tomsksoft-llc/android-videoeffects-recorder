package com.tomsksoft.videoeffectsrecorder.domain

fun interface CameraConfigurer<T: Camera<*>> {
    fun configure(camera: T, config: CameraConfig)
}