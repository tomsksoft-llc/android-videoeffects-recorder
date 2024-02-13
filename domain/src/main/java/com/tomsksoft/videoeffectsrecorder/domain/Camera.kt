package com.tomsksoft.videoeffectsrecorder.domain

interface Camera<F: Any>: FrameProvider<F> {

    var isEnabled: Boolean

    fun configure(config: CameraConfig)
}