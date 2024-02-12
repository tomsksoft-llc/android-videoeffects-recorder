package com.tomsksoft.videoeffectsrecorder.domain

interface VideoRecorder<F>: OnFrameListener<F> {
    /**
     * Record will be stopped after {@link java.lang.AutoCloseable#close()} invocation
     */
    fun startRecord(): Record<F>

    interface Record<F>: OnFrameListener<F>, AutoCloseable
}