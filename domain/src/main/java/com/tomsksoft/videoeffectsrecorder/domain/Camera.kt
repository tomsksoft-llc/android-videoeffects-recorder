package com.tomsksoft.videoeffectsrecorder.domain

interface Camera<F> {

    var isEnabled: Boolean

    fun configure(config: VideoConfig)

    fun subscribe(listener: OnFrameListener<F>)

    fun interface OnFrameListener<F> {
        fun onFrame(frame: F)
    }
}