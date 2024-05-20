package com.tomsksoft.videoeffectsrecorder.domain.boundary

import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig
import com.tomsksoft.videoeffectsrecorder.domain.entity.FlashMode
import io.reactivex.rxjava3.core.Observable

interface Camera {
    var isEnabled: Boolean // Shows if frame source (be it CameraX directly or CameraPipeline instance) is set up

    /**
     * Degree in {0, 90, 180, 270}
     */
    val orientation: Int
    val frame: Observable<Any>

    fun setFlashEnabled(enabled: Boolean)
    fun setDirection(direction: Direction)
    fun configure(cameraConfig: CameraConfig)

    enum class Direction {
        FRONT,
        BACK
    }
}