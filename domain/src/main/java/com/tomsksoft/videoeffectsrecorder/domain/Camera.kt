package com.tomsksoft.videoeffectsrecorder.domain

interface Camera {
    val orientation: Int
    /**
     * Shows if frame source (be it CameraX directly or CameraPipeline instance) is set up
     */
    var isEnabled: Boolean
    var direction: Direction

    enum class Direction {
        FRONT,
        BACK
    }
}