package com.tomsksoft.videoeffectsrecorder.domain

interface Camera<F> {

    var isEnabled: Boolean

    fun subscribe(listener: OnFrameListener<F>)

    fun unsubscribe(listener: OnFrameListener<F>)

    fun interface OnFrameListener<F> {
        fun onFrame(frame: F)
    }
}