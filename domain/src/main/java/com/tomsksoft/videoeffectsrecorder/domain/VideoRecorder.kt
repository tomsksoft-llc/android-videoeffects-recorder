package com.tomsksoft.videoeffectsrecorder.domain

import io.reactivex.rxjava3.subjects.Subject

interface VideoRecorder: FrameProvider {

    override val frame: Subject<Any>
    override val degree: Subject<Int>

    /**
     * Record will be stopped after {@link java.lang.AutoCloseable#close()} invocation
     */
    fun startRecord(filename: String, extension: String, mimeType: String): AutoCloseable
}