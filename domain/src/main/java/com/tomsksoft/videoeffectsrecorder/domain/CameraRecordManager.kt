package com.tomsksoft.videoeffectsrecorder.domain

class CameraRecordManager(
    camera: Camera,
    private val videoRecorder: VideoRecorder
) {
    companion object {
        const val BASE_NAME = "effects"
        const val MIME_TYPE = "video/mp4"
    }

    private var record: AutoCloseable? = null

    init {
        camera.frame.subscribe(videoRecorder.frame)
        camera.degree.subscribe(videoRecorder.degree)
    }

    var isRecording: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) startRecord() else stopRecord()
        }

    private fun startRecord() {
        record = videoRecorder.startRecord(BASE_NAME, MIME_TYPE)
    }

    private fun stopRecord() {
        record!!.close()
    }
}