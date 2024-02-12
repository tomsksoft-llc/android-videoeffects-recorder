package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.OnFrameListener
import com.tomsksoft.videoeffectsrecorder.domain.VideoRecorder

class CameraRecordManager<T: Camera<F>, F>(
    camera: T,
    private val videoRecorder: VideoRecorder<F>
): OnFrameListener<F> by videoRecorder {

    private var record: VideoRecorder.Record<F>? = null

    var isRecording: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) startRecord() else stopRecord()
        }

    var camera: T = camera
        set(value) {
            if (isRecording) {
                field.unsubscribe(this)
                value.subscribe(this)
            }
            field = value
        }

    private fun startRecord() {
        camera.subscribe(this)
        record = videoRecorder.startRecord()
    }

    private fun stopRecord() {
        record!!.close()
        camera.unsubscribe(this)
    }
}