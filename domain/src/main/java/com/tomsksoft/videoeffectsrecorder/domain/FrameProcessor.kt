package com.tomsksoft.videoeffectsrecorder.domain

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer

interface FrameProcessor {
    val frameSource: Observer<Any>
    val processedFrame: Observable<Any>

    fun configure(cameraConfig: CameraConfig)
}