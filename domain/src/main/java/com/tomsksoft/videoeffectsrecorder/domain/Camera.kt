package com.tomsksoft.videoeffectsrecorder.domain

interface Camera: FrameProvider {

    var isEnabled: Boolean
    var direction: Direction

    fun configure(config: CameraConfig)

    enum class Direction {
        FRONT,
        BACK
    }
}