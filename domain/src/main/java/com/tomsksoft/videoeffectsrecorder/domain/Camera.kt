package com.tomsksoft.videoeffectsrecorder.domain

interface Camera {
    val orientation: Int
    var flashMode: FlashMode //
    var isEnabled: Boolean // Shows if frame source (be it CameraX directly or CameraPipeline instance) is set up
    var direction: Direction
    var isFlashEnabled: Boolean

    enum class Direction {
        FRONT,
        BACK
    }
}