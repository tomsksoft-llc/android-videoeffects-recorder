package com.tomsksoft.videoeffectsrecorder.domain

interface PhotoPicker {
    fun takePhoto(frame: Any, orientation: Int, filename: String, mimeType: String)
}