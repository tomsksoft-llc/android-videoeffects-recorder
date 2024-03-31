package com.tomsksoft.videoeffectsrecorder.domain

import android.view.Surface
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Observer

interface FrameProcessor {
    val frameSource: Observer<Any>

    fun setSurface(surface: Surface?)
    fun configure(cameraConfig: CameraConfig)
}