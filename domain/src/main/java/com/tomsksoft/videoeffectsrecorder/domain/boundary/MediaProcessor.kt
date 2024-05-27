package com.tomsksoft.videoeffectsrecorder.domain.boundary

import android.view.Surface

interface MediaProcessor {
    fun processImage()
    fun addMedia(uri: String)
    fun setSurface(surface: Surface?)
}