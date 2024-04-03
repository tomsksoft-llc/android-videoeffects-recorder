package com.tomsksoft.videoeffectsrecorder.domain

class CameraRecordManager(
    private val cameraManager: CameraManager,
    private val videoRecorder: VideoRecorder
) {
    companion object {
        const val BASE_NAME = "effects"
        const val MIME_TYPE = "video/mp4"
    }

    private var record: AutoCloseable? = null

    var isRecording: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) startRecord() else stopRecord()
        }

    private fun startRecord() {
        record = videoRecorder.startRecord(
            cameraManager.frameSource,
            cameraManager.orientation,
            BASE_NAME,
            MIME_TYPE
        )
    }

    private fun stopRecord() {
        record?.close()
        record = null
    }
}