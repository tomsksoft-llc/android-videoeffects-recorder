package com.tomsksoft.videoeffectsrecorder.domain.boundary

import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.entity.FlashMode
import io.reactivex.rxjava3.core.Observable

interface Camera {
    val orientation: Int
    var flashMode: FlashMode
    var isEnabled: Boolean // Shows if frame source (be it CameraX directly or CameraPipeline instance) is set up
    var direction: Direction
    var isFlashEnabled: Boolean
    val frame: Observable<Any>

    fun configure(cameraConfig: CameraConfig)

    enum class Direction {
        FRONT,
        BACK
    }
}