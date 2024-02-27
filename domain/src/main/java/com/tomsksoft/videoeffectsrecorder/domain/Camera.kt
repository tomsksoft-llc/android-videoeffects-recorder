package com.tomsksoft.videoeffectsrecorder.domain

interface Camera: FrameProvider {

    var isEnabled: Boolean

    fun configure(config: CameraConfig)
}