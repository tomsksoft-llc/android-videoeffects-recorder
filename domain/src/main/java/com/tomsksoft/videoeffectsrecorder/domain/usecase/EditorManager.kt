package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.boundary.MediaProcessor

class EditorManager (
    val mediaProcessor: MediaProcessor
) {
    fun addMedia(uri: String) {
        mediaProcessor.addMedia(uri)
    }
}