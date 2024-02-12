package com.tomsksoft.videoeffectsrecorder.domain

fun interface OnFrameListener<F> {
    fun onFrame(frame: F)
}