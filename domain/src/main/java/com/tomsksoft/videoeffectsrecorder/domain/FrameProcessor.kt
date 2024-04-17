package com.tomsksoft.videoeffectsrecorder.domain

import android.view.Surface
import io.reactivex.rxjava3.core.Observable

interface FrameProcessor {
    val processedFrame: Observable<Any>

    fun setSurface(surface: Surface?)
    fun configure(cameraConfig: CameraConfig)
}