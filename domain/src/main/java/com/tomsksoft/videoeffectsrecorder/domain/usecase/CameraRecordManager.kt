package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.boundary.PhotoPicker
import com.tomsksoft.videoeffectsrecorder.domain.boundary.VideoRecorder
import com.tomsksoft.videoeffectsrecorder.domain.entity.FlashMode
import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    val isRecording: Boolean get() = record != null

    fun takePhoto(frameSource: Observable<Any>): Int {
        val flashDuration =
            if (cameraManager.flashMode.blockingFirst() == FlashMode.AUTO)
                1000
            else
                0

        scope.launch {
            val frame: Any

            if (flashDuration > 0) {
                cameraManager.setFlashEnabled(true)
                delay(flashDuration.toLong())
                frame = frameSource.blockingFirst()
                cameraManager.setFlashEnabled(false)
            } else
                frame = frameSource.blockingFirst()

            photoPicker.takePhoto(
                frame,
                cameraManager.orientation.blockingFirst(),
                PHOTO_BASE_NAME,
                PHOTO_MIME_TYPE
            )
        }

        return flashDuration
    }

    fun startRecord(frameSource: Observable<Any>) {
        record = videoRecorder.startRecord(
            frameSource,
            cameraManager.orientation.blockingFirst(),
            VIDEO_BASE_NAME,
            VIDEO_MIME_TYPE
        )
    }

    fun stopRecord() {
        record?.close()
        record = null
    }
}