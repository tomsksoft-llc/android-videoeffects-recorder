package com.tomsksoft.videoeffectsrecorder.domain.mock

import com.tomsksoft.videoeffectsrecorder.domain.boundary.VideoRecorder
import io.reactivex.rxjava3.core.Observable

class VideoRecorderMock: VideoRecorder {

    var isRecording = false

    override fun startRecord(
        frameSource: Observable<Any>,
        orientation: Int,
        filename: String,
        mimeType: String
    ): AutoCloseable {
        isRecording = true
        return AutoCloseable { isRecording = false }
    }
}