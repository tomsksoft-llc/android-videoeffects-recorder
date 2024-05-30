package com.tomsksoft.videoeffectsrecorder.domain.boundary

import com.tomsksoft.videoeffectsrecorder.domain.entity.CameraConfig

interface MediaProcessor {
    fun processImage()
    fun addMedia(uri: String)
    fun configure(cameraConfig: CameraConfig)
}