package com.tomsksoft.videoeffectsrecorder.domain

// can it be responsible for recording video?
interface VideoRepository<F>: Camera.OnFrameListener<F> {
    // TODO [tva] provide list of records and their metadata

    /**
     * Record will be stopped after {@link java.lang.AutoCloseable#close()} invocation
     */
    fun startRecord(): Record<F>

    interface Record<F>: Camera.OnFrameListener<F>, AutoCloseable
}