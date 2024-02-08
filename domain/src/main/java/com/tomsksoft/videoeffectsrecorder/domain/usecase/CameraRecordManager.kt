package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.VideoRepository

class CameraRecordManager<T: Camera<F>, F>(
    camera: T,
    private val videoRepository: VideoRepository<F>
): Camera.OnFrameListener<F> by videoRepository {

    private var record: VideoRepository.Record<F>? = null

    val isRecording: Boolean
        get() = record != null

    var camera: T = camera
        set(value) {
            if (isRecording) {
                field.unsubscribe(this)
                value.subscribe(this)
            }
            field = value
        }

    fun startRecord() {
        if (isRecording)
            throw IllegalStateException("Couldn't start record: Video is already recording")
        camera.subscribe(this)
        videoRepository.startRecord()
    }

    fun stopRecording() {
        if (!isRecording)
            throw IllegalStateException("Couldn't stop record: Video isn't recording")
        record!!.close()
        camera.unsubscribe(this)
    }
}