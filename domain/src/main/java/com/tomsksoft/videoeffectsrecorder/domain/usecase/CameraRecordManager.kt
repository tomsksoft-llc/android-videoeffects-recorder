package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.boundary.PhotoPicker
import com.tomsksoft.videoeffectsrecorder.domain.boundary.VideoRecorder
import com.tomsksoft.videoeffectsrecorder.domain.entity.FlashMode
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.subjects.PublishSubject
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

    val isRecording: Boolean get() = record != null

    fun takePhoto(frameSource: Observable<Any>): Single<*> =
        Observable.create { frameEmitter ->
            fun emit() {
                val frame = frameSource.blockingFirst()
                frameEmitter.onNext(frame)
                frameEmitter.onComplete()

                photoPicker.takePhoto(
                    frame,
                    cameraManager.orientation.blockingFirst(),
                    PHOTO_BASE_NAME,
                    PHOTO_MIME_TYPE
                )
            }

            if (cameraManager.flashMode.blockingFirst() == FlashMode.AUTO) {
                cameraManager.setFlashEnabled(true)
                Thread.sleep(1000)
                emit()
                cameraManager.setFlashEnabled(false)
            } else emit()
        }.singleOrError()

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