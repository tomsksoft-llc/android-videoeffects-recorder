package com.tomsksoft.videoeffectsrecorder.domain

import io.reactivex.rxjava3.core.Observable

interface VideoRecorder {
    /**
     * Record will be stopped after {@link java.lang.AutoCloseable#close()} invocation
     * @param orientation degree in 0..359
     */
    fun startRecord(
        frameSource: Observable<Any>,
        orientation: Int,
        filename: String,
        mimeType: String
    ): AutoCloseable
}