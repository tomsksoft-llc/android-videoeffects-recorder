package com.tomsksoft.videoeffectsrecorder.domain

import io.reactivex.rxjava3.subjects.Subject
import java.io.File

interface VideoRecorder<F: Any>: FrameProvider<F> {

    override val frame: Subject<F>
    override val degree: Subject<Int>

    /**
     * Record will be stopped after {@link java.lang.AutoCloseable#close()} invocation
     */
    fun startRecord(filename: String, extension: String, mimeType: String): AutoCloseable
}