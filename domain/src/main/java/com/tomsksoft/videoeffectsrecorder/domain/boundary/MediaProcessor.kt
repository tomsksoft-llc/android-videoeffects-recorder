package com.tomsksoft.videoeffectsrecorder.domain.boundary

interface MediaProcessor {
    fun processImage()
    fun addMedia(uri: String)
}