package com.tomsksoft.videoeffectsrecorder.ui.viewmodel

import android.view.Surface

interface EditMediaViewModel {
    fun updateSurface(surface: Surface?)
    fun addMedia(uri: String)
    fun processImage()
}