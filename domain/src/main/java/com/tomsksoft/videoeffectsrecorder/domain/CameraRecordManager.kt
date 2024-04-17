package com.tomsksoft.videoeffectsrecorder.domain

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CameraRecordManager(
    private val cameraManager: CameraManager,
    private val videoRecorder: VideoRecorder,
    private val photoPicker: PhotoPicker
) {
    companion object {
        const val VIDEO_BASE_NAME = "effects"
        const val VIDEO_MIME_TYPE = "video/mp4"
        const val PHOTO_BASE_NAME = "effects"
        const val PHOTO_MIME_TYPE = "image/jpeg"
    }

    private var record: AutoCloseable? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    var isRecording: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) startRecord() else stopRecord()
        }

    fun takePhoto() {
        val frame = cameraManager.frameSource.blockingFirst()
        scope.launch {
            photoPicker.takePhoto(
                frame,
                cameraManager.orientation,
                PHOTO_BASE_NAME,
                PHOTO_MIME_TYPE
            )
        }
    }

    private fun startRecord() {
        record = videoRecorder.startRecord(
            cameraManager.frameSource,
            cameraManager.orientation,
            VIDEO_BASE_NAME,
            VIDEO_MIME_TYPE
        )
    }

    private fun stopRecord() {
        record?.close()
        record = null
    }
}