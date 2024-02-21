package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.FileStore
import com.tomsksoft.videoeffectsrecorder.domain.FrameProvider
import com.tomsksoft.videoeffectsrecorder.domain.VideoRecorder
import io.reactivex.rxjava3.core.Observable

/**
 * @param C camera
 * @param F frame
 * @param T file
 */
class CameraRecordManager<C: Camera<F>, F: Any, T>(
    camera: C,
    private val fileStore: FileStore<T>,
    private val videoRecorder: VideoRecorder<F, T>
): FrameProvider<F> {
    companion object {
        const val BASE_NAME = "effects"
        const val EXTENSION = "mp4"
        const val MIME_TYPE = "video/mp4"
    }

    private var record: VideoRecorder.Record? = null

    var camera: C = camera
        set(value) {
            field = value
            frame.subscribe(videoRecorder.frame)
            degree.subscribe(videoRecorder.degree)
        }

    init { this.camera = camera } // invoke setter to subscribe

    override val frame: Observable<F>
        get() = camera.frame

    override val degree: Observable<Int>
        get() = camera.degree

    var isRecording: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) startRecord() else stopRecord()
        }

    private fun startRecord() {
        val outputFile = fileStore.create(BASE_NAME, EXTENSION, MIME_TYPE)
        record = videoRecorder.startRecord(outputFile)
    }

    private fun stopRecord() {
        record!!.close()
    }
}