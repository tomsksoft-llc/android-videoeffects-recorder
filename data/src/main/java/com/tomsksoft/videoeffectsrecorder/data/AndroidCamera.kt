package com.tomsksoft.videoeffectsrecorder.data

import android.view.Surface
import com.tomsksoft.videoeffectsrecorder.domain.boundary.Camera

interface AndroidCamera: Camera {
    fun setSurface(surface: Surface?)
}