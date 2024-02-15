package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.Camera
import com.tomsksoft.videoeffectsrecorder.domain.FrameProvider
import com.tomsksoft.videoeffectsrecorder.domain.VideoRecorder
import io.reactivex.rxjava3.core.Observable

class CameraRecordManager<T: Camera<F>, F: Any>(
    camera: T,
    private val fileManager: FileManager,
    private val videoRecorder: VideoRecorder<F>
): FrameProvider<F> {
    companion object {
        const val BASE_NAME = "effects"
        const val EXTENSION = "mp4"
    }

    private var record: VideoRecorder.Record? = null

    var camera: T = camera
        set(value) {
            field = value
            camera.frame.subscribe(videoRecorder.frame)
        }

    init { this.camera = camera } // invoke setter to subscribe

    override val frame: Observable<F>
        get() = camera.frame

    var isRecording: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            if (value) startRecord() else stopRecord()
        }

    private fun startRecord() {
        record = videoRecorder.startRecord(fileManager.create(BASE_NAME, EXTENSION))
    }

    private fun stopRecord() {
        record!!.close()
    }
}