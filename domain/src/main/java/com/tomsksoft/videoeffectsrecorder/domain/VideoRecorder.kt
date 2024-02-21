package com.tomsksoft.videoeffectsrecorder.domain

import io.reactivex.rxjava3.subjects.Subject
import java.io.File

/**
 * @param F frame
 * @param T file
 */
interface VideoRecorder<F: Any, T>: FrameProvider<F> {

    override val frame: Subject<F>
    override val degree: Subject<Int>

    /**
     * Record will be stopped after {@link java.lang.AutoCloseable#close()} invocation
     */
    fun startRecord(outputFile: T): Record

    interface Record: AutoCloseable
}