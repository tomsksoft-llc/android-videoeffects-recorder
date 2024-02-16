package com.tomsksoft.videoeffectsrecorder.domain.usecase

import android.util.Log
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
        const val MIME_TYPE = "video/mp4"
    }

    private var record: VideoRecorder.Record? = null

    var camera: T = camera
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
        val fileDescriptor = fileManager.create(BASE_NAME, EXTENSION, MIME_TYPE)
        record = videoRecorder.startRecord(fileDescriptor)
    }

    private fun stopRecord() {
        record!!.close()
    }
}