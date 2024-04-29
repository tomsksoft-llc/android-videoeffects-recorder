package com.tomsksoft.videoeffectsrecorder.domain.boundary

interface PhotoPicker {
    fun takePhoto(frame: Any, orientation: Int, filename: String, mimeType: String)
}