package com.tomsksoft.videoeffectsrecorder.domain.usecase

import com.tomsksoft.videoeffectsrecorder.domain.boundary.MediaProcessor

class EditorManager (
    private val mediaProcessor: MediaProcessor
) {
    fun addMedia(uri: String) {
        mediaProcessor.addMedia(uri)
    }

    fun processMedia() {
        mediaProcessor.processImage()
    }
}