package com.tomsksoft.videoeffectsrecorder.domain.mock

import com.tomsksoft.videoeffectsrecorder.domain.boundary.PhotoPicker
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class PhotoPickerMock: PhotoPicker {
    override fun takePhoto(frame: Any, orientation: Int, filename: String, mimeType: String) =
        runBlocking { delay(1000) }
}