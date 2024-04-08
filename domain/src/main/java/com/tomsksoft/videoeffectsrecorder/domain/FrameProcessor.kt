package com.tomsksoft.videoeffectsrecorder.domain

import android.view.Surface
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer

interface FrameProcessor {
    val frameSource: Observer<Any>
    val processedFrame: Observable<Any>
    var direction: Direction

    fun setSurface(surface: Surface?)
    fun configure(cameraConfig: CameraConfig)

    enum class Direction {
        BACK,
        FRONT
    }
}